package co.casterlabs.flv4j.rtmp;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.rtmp.chunks.RTMPChunk;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class ChunkStream {
    private final RTMPReader rtmp;
    private final ASReader reader;

    private int previousMessageLength;
    private int previousMessageTypeId;
    private long previousMessageStreamId;

    private ChunkInProgress inProgress;

    void abort() {
        this.inProgress = null;
    }

    @Nullable
    RTMPChunk<?> read(int format, int csId, int chunkSize) throws IOException {
        long timestamp;
        int messageLength;
        int messageTypeId;
        long messageStreamId;
        switch (format) {
            case 0: {
                // https://rtmp.veriskope.com/pdf/rtmp_specification_1.0.pdf#page=14
                timestamp = this.reader.u24();
                messageLength = this.reader.u24();
                messageTypeId = this.reader.u8();
                messageStreamId = this.reader.u32le();

                this.rtmp.incrementRead(11);

                if (timestamp == 0xFFFFFF) {
                    timestamp = this.reader.u32();
                    this.rtmp.incrementRead(4);
                }
                break;
            }

            case 1: {
                // https://rtmp.veriskope.com/pdf/rtmp_specification_1.0.pdf#page=14
                // (reuse messageStreamId)
                messageStreamId = this.previousMessageStreamId;

                long timestampDelta = this.reader.u24();
                messageLength = this.reader.u24();
                messageTypeId = this.reader.u8();

                this.rtmp.incrementRead(7);

                if (timestampDelta == 0xFFFFFF) {
                    timestampDelta = this.reader.u32();
                    this.rtmp.incrementRead(4);
                }

                timestamp = this.rtmp.previousTimestamp + timestampDelta;
                break;
            }

            case 2: {
                // https://rtmp.veriskope.com/pdf/rtmp_specification_1.0.pdf#page=15
                // (reuse everything except timestamp)
                messageLength = this.previousMessageLength;
                messageTypeId = this.previousMessageTypeId;
                messageStreamId = this.previousMessageStreamId;

                long timestampDelta = this.reader.u24();

                this.rtmp.incrementRead(3);

                if (timestampDelta == 0xFFFFFF) {
                    timestampDelta = this.reader.u32();
                    this.rtmp.incrementRead(4);
                }

                timestamp = this.rtmp.previousTimestamp + timestampDelta;
                break;
            }

            case 3:
                // https://rtmp.veriskope.com/pdf/rtmp_specification_1.0.pdf#page=15
                // (reuse all previous values)
                timestamp = this.rtmp.previousTimestamp;
                messageLength = this.previousMessageLength;
                messageTypeId = this.previousMessageTypeId;
                messageStreamId = this.previousMessageStreamId;
                break;

            default: // Silence the compiler.
                throw new IllegalStateException();
        }

        int timestamp31 = (int) (timestamp & 0x7FFFFFFFL);

        this.rtmp.previousTimestamp = timestamp31;
        this.previousMessageLength = messageLength;
        this.previousMessageTypeId = messageTypeId;
        this.previousMessageStreamId = messageStreamId;

        // 2 is the abort message, we need to parse that FULLY even if the stream is in
        // the middle of a chunk.

        RTMPMessage message;
        if (messageLength > chunkSize && messageTypeId != 2) {
            if (this.inProgress == null) {
                this.inProgress = new ChunkInProgress(messageLength);
            }

            int maxToRead = Math.min(chunkSize, this.inProgress.remaining());
            this.rtmp.incrementRead(maxToRead);
            if (this.inProgress.append(this.reader.bytes(maxToRead))) {
                return null;
            }

            message = RTMPMessage.parse(messageTypeId, this.inProgress.buffer.length, new ASReader(this.inProgress.buffer));
            this.inProgress = null;
        } else {
            message = RTMPMessage.parse(messageTypeId, messageLength, this.reader);
        }

        return new RTMPChunk<>(timestamp31, csId, messageTypeId, messageStreamId, message);
    }

}
