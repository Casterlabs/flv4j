package co.casterlabs.flv4j.rtmp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.rtmp.chunks.RTMPChunk;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessageAbort;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessageChunkSize;
import co.casterlabs.flv4j.rtmp.handshake.RTMPHandshake0;
import co.casterlabs.flv4j.rtmp.handshake.RTMPHandshake1;
import co.casterlabs.flv4j.rtmp.handshake.RTMPHandshake2;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

// https://rtmp.veriskope.com/pdf/rtmp_specification_1.0.pdf
@RequiredArgsConstructor
public class RTMPReader {
    private final ASReader reader;
    private final Map<Integer, ChunkStream> chunkStreams = new HashMap<>();

    private int previousTimestamp;
    private int chunkSize = 128;

    private @Setter int windowAcknowledgementSize = -1;
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

        ChunkStream cs = this.chunkStreams.get(csId);
        if (cs == null) {
            cs = new ChunkStream(this, this.reader);
            this.chunkStreams.put(csId, cs);
        }

        RTMPChunk<?> chunk = cs.read(this.previousTimestamp, format, csId, this.chunkSize);
        if (chunk == null) {
            return null;
        }

        this.previousTimestamp = chunk.timestamp();

        if (chunk.message() instanceof RTMPMessageAbort abort) {
            ChunkStream stream = this.chunkStreams.get((int) abort.streamId());
            if (stream != null) {
                stream.abort();
            }
            return null;
        } else if (chunk.message() instanceof RTMPMessageChunkSize chunkMessage) {
            this.chunkSize = chunkMessage.chunkSize();
        }

        return chunk;
    }

}
