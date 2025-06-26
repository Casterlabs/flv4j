package co.casterlabs.flv4j.rtmp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.rtmp.chunks.RTMPChunk;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessageChunkSize;
import co.casterlabs.flv4j.rtmp.handshake.RTMPHandshake0;
import co.casterlabs.flv4j.rtmp.handshake.RTMPHandshake1;
import co.casterlabs.flv4j.rtmp.handshake.RTMPHandshake2;
import lombok.RequiredArgsConstructor;

// https://rtmp.veriskope.com/pdf/rtmp_specification_1.0.pdf
@RequiredArgsConstructor
public class RTMPReader {
    private final ASReader reader;

    private int previousTimestamp;
    private int chunkSize = 128;
    private Map<Integer, ChunkStream> chunkStreams = new HashMap<>();

    public RTMPHandshake0 handshake0() throws IOException {
        return RTMPHandshake0.parse(this.reader);
    }

    public RTMPHandshake1 handshake1() throws IOException {
        return RTMPHandshake1.parse(this.reader);
    }

    public RTMPHandshake2 handshake2() throws IOException {
        return RTMPHandshake2.parse(this.reader);
    }

    public RTMPChunk<?> read() throws IOException {
        // https://rtmp.veriskope.com/pdf/rtmp_specification_1.0.pdf#page=13
        int fb = this.reader.u8();

        int format = fb >> 6 & 0b11;
        int csId = fb & 0b00111111;

        if (csId == 0) {
            csId = this.reader.u8() + 64;
        } else if (csId == 1) {
            int b2 = this.reader.u8();
            int b3 = this.reader.u8();
            csId = (b3 * 256) + (b2 + 64);
        }

        ChunkStream cs = this.chunkStreams.get(csId);
        if (cs == null) {
            cs = new ChunkStream(this.reader);
            this.chunkStreams.put(csId, cs);
        }

        RTMPChunk<?> chunk = cs.read(this.previousTimestamp, format, csId, this.chunkSize);
        if (chunk == null) {
            return null;
        }

        this.previousTimestamp = chunk.timestamp();

        if (chunk.message() instanceof RTMPMessageChunkSize chunkMessage) {
            this.chunkSize = chunkMessage.chunkSize();
        }

        return chunk;
    }

}
