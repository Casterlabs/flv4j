package co.casterlabs.flv4j.rtmp;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import co.casterlabs.flv4j.actionscript.io.ASWriter;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessage;
import co.casterlabs.flv4j.rtmp.handshake.RTMPHandshake0;
import co.casterlabs.flv4j.rtmp.handshake.RTMPHandshake1;
import co.casterlabs.flv4j.rtmp.handshake.RTMPHandshake2;
import lombok.RequiredArgsConstructor;

// https://rtmp.veriskope.com/pdf/rtmp_specification_1.0.pdf
@RequiredArgsConstructor
public class RTMPWriter {
    private final ASWriter writer;

    private byte[] handshakeRandom = new byte[RTMPHandshake1.RANDOM_SIZE];
    private long epoch = System.currentTimeMillis();
    private int chunkSize = 128;

    public void handshake0() throws IOException {
        new RTMPHandshake0(3).serialize(this.writer);
    }

    public void handshake1() throws IOException {
        ThreadLocalRandom.current().nextBytes(this.handshakeRandom);
        new RTMPHandshake1(0, this.handshakeRandom).serialize(this.writer);
    }

    public void handshake2(RTMPHandshake1 other) throws IOException {
        new RTMPHandshake2(
            other.epoch(),
            0,
            other.random()
        ).serialize(this.writer);
    }

    public boolean validateHandshake2(RTMPHandshake2 hs) {
        return Arrays.equals(hs.randomEcho(), this.handshakeRandom);
    }

    public void write(int chunkStreamId, long messageStreamId, RTMPMessage message) throws IOException {
        int size = message.size();
        this.writeChunkHeader(0, chunkStreamId, messageStreamId, size, message.rawType());

        if (message.size() > this.chunkSize) {
            // Chunking!
            byte[] b = message.raw();

            this.writer.bytes(b, 0, this.chunkSize);

            int offset = this.chunkSize;
            while (b.length - offset > 0) {
                this.writeChunkHeader(3, chunkStreamId, messageStreamId, size, message.rawType());
                int toWrite = Math.min(b.length - offset, this.chunkSize);
                this.writer.bytes(b, offset, toWrite);
                offset += toWrite;
            }
        } else {
            message.serialize(this.writer);
        }
    }

    private void writeChunkHeader(int format, int csId, long msId, int messageLength, int messageTypeId) throws IOException {
        int fb = format << 6 | (csId & 0b00111111); // TODO 2/3 byte extensions
        this.writer.u8(fb);

        int now = (int) ((this.epoch - System.currentTimeMillis()) & 0xFFFFFF);

        switch (format) {
            case 0:
                // https://rtmp.veriskope.com/pdf/rtmp_specification_1.0.pdf#page=14
                this.writer.u24(now);
                this.writer.u24(messageLength);
                this.writer.u8(messageTypeId);
                this.writer.u32le(msId);
                break;

            case 3:
                // https://rtmp.veriskope.com/pdf/rtmp_specification_1.0.pdf#page=15
                // (reuse all previous values)
                break;

        }
    }

}
