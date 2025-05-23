package co.casterlabs.flv4j.rtmp.handshake;

import java.io.IOException;

import co.casterlabs.flv4j.FLVSerializable;
import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASSizer;
import co.casterlabs.flv4j.actionscript.io.ASWriter;

// https://rtmp.veriskope.com/pdf/rtmp_specification_1.0.pdf#page=8
public record RTMPHandshake1(
    long epoch,
    byte[] random
) implements FLVSerializable {
    public static final int RANDOM_SIZE = 1528;
    private static final int SIZE = new ASSizer().u32().u32().bytes(RANDOM_SIZE).size;

    @Override
    public int size() {
        return SIZE;
    }

    @Override
    public void serialize(ASWriter writer) throws IOException {
        writer.u32(this.epoch);
        writer.u32(0); // zero
        writer.bytes(this.random);
    }

    public static RTMPHandshake1 parse(ASReader reader) throws IOException {
        long epoch = reader.u32();
        reader.u32(); // zero
        byte[] random = reader.bytes(RANDOM_SIZE);
        return new RTMPHandshake1(epoch, random);
    }

}
