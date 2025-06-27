package co.casterlabs.flv4j.rtmp.chunks;

import java.io.IOException;

import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASSizer;
import co.casterlabs.flv4j.actionscript.io.ASWriter;

// https://rtmp.veriskope.com/pdf/rtmp_specification_1.0.pdf#page=19
/**
 * Used to notify the peer of a new maximum chunk size. The maximum chunk size
 * defaults to 128 bytes, but the client or the server can change this value,
 * and updates its peer using this message. For example, suppose a client wants
 * to send 131 bytes of audio data and the chunk size is 128. In this case, the
 * client can send this message to the server to notify it that the chunk size
 * is now 131 bytes. The client can then send the audio data in a single chunk.
 * 
 * @apiNote The maximum chunk size SHOULD be at least 128 bytes, and MUST be at
 *          least 1 byte. The maximum chunk size is maintained independently for
 *          each direction.
 */
public record RTMPMessageChunkSize(int chunkSize) implements RTMPMessage {
    private static final int SIZE = new ASSizer().u32().size;

    @Override
    public boolean isControl() {
        return true;
    }

    @Override
    public int rawType() {
        return 1;
    }

    @Override
    public int size() {
        return SIZE;
    }

    @Override
    public void serialize(ASWriter writer) throws IOException {
        writer.u32(this.chunkSize);
    }

    public static RTMPMessageChunkSize parse(ASReader reader) throws IOException {
        int chunkSize = (int) reader.u32();
        return new RTMPMessageChunkSize(chunkSize);
    }

}
