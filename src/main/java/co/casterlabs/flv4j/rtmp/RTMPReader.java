package co.casterlabs.flv4j.rtmp;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.rtmp.chunks.RTMPChunk;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessage;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessageAbort;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessageChunkSize;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessageWindowAcknowledgementSize;
import co.casterlabs.flv4j.rtmp.handshake.RTMPHandshake0;
import co.casterlabs.flv4j.rtmp.handshake.RTMPHandshake1;
import co.casterlabs.flv4j.rtmp.handshake.RTMPHandshake2;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

// https://rtmp.veriskope.com/pdf/rtmp_specification_1.0.pdf
@RequiredArgsConstructor
public class RTMPReader {
    private final ASReader reader;
    private ChunkStream[] chunkStreams = {}; // NB: [0] and [1] are illegal values (they indicate extended id format)

    int previousTimestamp;
    private int chunkSize = 128;

    private @Setter long windowAcknowledgementSize = -1;
    private long lastAckAt = 0;
    private long read = 0;

    void incrementRead(int amount) {
        this.read += amount;
        if (this.read > 0xFFFFFFFFL) {
            this.read %= 0xFFFFFFFFL; // wrap around after 32 bits.
        }
    }

    public RTMPHandshake0 handshake0() throws IOException {
        return RTMPHandshake0.parse(this.reader);
    }

    public RTMPHandshake1 handshake1() throws IOException {
        return RTMPHandshake1.parse(this.reader);
    }

    public RTMPHandshake2 handshake2() throws IOException {
        return RTMPHandshake2.parse(this.reader);
    }

    public boolean needsAck() {
        if (this.windowAcknowledgementSize <= 0) return false;
        return this.read - this.lastAckAt > this.windowAcknowledgementSize / 2;
    }

    public long ackSeq() {
        this.lastAckAt = this.read;
        return this.read;
    }

    public RTMPChunk<?> read() throws IOException {
        // https://rtmp.veriskope.com/pdf/rtmp_specification_1.0.pdf#page=13
        int fb = this.reader.u8();
        this.incrementRead(1);

        int format = fb >> 6 & 0b11;
        int csId = fb & 0b00111111;

        if (csId == 0) {
            csId = this.reader.u8() + 64;
            this.incrementRead(1);
        } else if (csId == 1) {
            int b2 = this.reader.u8();
            int b3 = this.reader.u8();
            csId = (b3 * 256) + (b2 + 64);
            this.incrementRead(2);
        }

        if (csId >= this.chunkStreams.length) {
            ChunkStream[] newArr = new ChunkStream[csId + 1];
            System.arraycopy(this.chunkStreams, 0, newArr, 0, this.chunkStreams.length);
            this.chunkStreams = newArr;
        }
        ChunkStream cs = this.chunkStreams[csId];
        if (cs == null) {
            cs = this.chunkStreams[csId] = new ChunkStream();
        }

        RTMPChunk<?> chunk = cs.read(format, csId, this.chunkSize);
        if (chunk == null) {
            return null;
        }

        this.previousTimestamp = chunk.timestamp();

        if (chunk.message() instanceof RTMPMessageAbort abort) {
            ChunkStream stream = this.chunkStreams[(int) abort.streamId()];
            if (stream != null) {
                stream.inProgress = null;
            }
            return null;
        } else if (chunk.message() instanceof RTMPMessageChunkSize chunkMessage) {
            this.chunkSize = chunkMessage.chunkSize();
        } else if (chunk.message() instanceof RTMPMessageWindowAcknowledgementSize size) {
            this.windowAcknowledgementSize = size.windowSize();
        }

        return chunk;
    }

    class ChunkStream {
        private int previousMessageLength;
        private int previousMessageTypeId;
        private long previousMessageStreamId;
        private int previousDelta = 0;

        private ChunkInProgress inProgress;

        @Nullable
        RTMPChunk<?> read(int format, int csId, int chunkSize) throws IOException {
            long timestamp;
            int messageLength;
            int messageTypeId;
            long messageStreamId;
            switch (format) {
                case 0: {
                    // https://rtmp.veriskope.com/pdf/rtmp_specification_1.0.pdf#page=14
                    timestamp = reader.u24();
                    messageLength = reader.u24();
                    messageTypeId = reader.u8();
                    messageStreamId = reader.u32le();

                    incrementRead(11);

                    if (timestamp == 0xFFFFFF) {
                        timestamp = reader.u32();
                        incrementRead(4);
                    }

                    this.previousDelta = 0;
                    break;
                }

                case 1: {
                    // https://rtmp.veriskope.com/pdf/rtmp_specification_1.0.pdf#page=14
                    // (reuse messageStreamId)
                    messageStreamId = this.previousMessageStreamId;

                    long timestampDelta = reader.u24();
                    messageLength = reader.u24();
                    messageTypeId = reader.u8();

                    incrementRead(7);

                    if (timestampDelta == 0xFFFFFF) {
                        timestampDelta = reader.u32();
                        incrementRead(4);
                    }

                    this.previousDelta = (int) timestampDelta;
                    timestamp = previousTimestamp + timestampDelta;
                    break;
                }

                case 2: {
                    // https://rtmp.veriskope.com/pdf/rtmp_specification_1.0.pdf#page=15
                    // (reuse everything except timestamp)
                    messageLength = this.previousMessageLength;
                    messageTypeId = this.previousMessageTypeId;
                    messageStreamId = this.previousMessageStreamId;

                    long timestampDelta = reader.u24();

                    incrementRead(3);

                    if (timestampDelta == 0xFFFFFF) {
                        timestampDelta = reader.u32();
                        incrementRead(4);
                    }

                    this.previousDelta = (int) timestampDelta;
                    timestamp = previousTimestamp + timestampDelta;
                    break;
                }

                case 3:
                    // https://rtmp.veriskope.com/pdf/rtmp_specification_1.0.pdf#page=15
                    // (reuse all previous values)

                    // NB: Documentation missing critical information: "Reuse all previous values"
                    // also means reuse the timestamp delta if the previous chunk in the stream had
                    // a delta.

                    // So for type0 -> type3, we reuse the whole timestamp (implicit delta of 0)
                    // For type1/2 -> type3, we use their delta.

                    timestamp = previousTimestamp + this.previousDelta;
                    messageLength = this.previousMessageLength;
                    messageTypeId = this.previousMessageTypeId;
                    messageStreamId = this.previousMessageStreamId;
                    break;

                default: // Silence the compiler.
                    throw new IllegalStateException();
            }

            int timestamp31 = (int) (timestamp & 0x7FFFFFFFL);

            previousTimestamp = timestamp31;
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
                incrementRead(maxToRead);
                if (this.inProgress.append(reader.bytes(maxToRead))) {
                    return null;
                }

                message = RTMPMessage.parse(messageTypeId, this.inProgress.buffer.length, new ASReader(this.inProgress.buffer));
                this.inProgress = null;
            } else {
                message = RTMPMessage.parse(messageTypeId, messageLength, reader);
            }

            return new RTMPChunk<>(timestamp31, csId, messageTypeId, messageStreamId, message);
        }

    }

    private static class ChunkInProgress {
        final byte[] buffer;
        private int writeOffset = 0;

        ChunkInProgress(int length) {
            this.buffer = new byte[length];
        }

        int remaining() {
            return this.buffer.length - this.writeOffset;
        }

        /**
         * @return true if more data is needed.
         */
        boolean append(byte[] bytes) {
            System.arraycopy(bytes, 0, this.buffer, this.writeOffset, bytes.length);
            this.writeOffset += bytes.length;

            return this.writeOffset < this.buffer.length;
        }

    }

}
