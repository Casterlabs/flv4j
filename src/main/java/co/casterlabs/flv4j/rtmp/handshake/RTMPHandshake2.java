package co.casterlabs.flv4j.rtmp.handshake;

import java.io.IOException;

import co.casterlabs.flv4j.FLVSerializable;
import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASSizer;
import co.casterlabs.flv4j.actionscript.io.ASWriter;

// https://rtmp.veriskope.com/pdf/rtmp_specification_1.0.pdf#page=9
public record RTMPHandshake2(
    long epoch,
    long timeReceived,
    byte[] randomEcho
) implements FLVSerializable {

    private static final int SIZE = new ASSizer().u32().u32().bytes(RTMPHandshake1.RANDOM_SIZE).size;

    @Override
    public int size() {
        return SIZE;
    }

    @Override
    public void serialize(ASWriter writer) throws IOException {
        writer.u32(this.epoch);
        writer.u32(this.timeReceived);
        writer.bytes(this.randomEcho);
    }

    public static RTMPHandshake2 parse(ASReader reader) throws IOException {
        long epoch = reader.u32();
        long timeReceived = reader.u32();
        byte[] random = reader.bytes(RTMPHandshake1.RANDOM_SIZE);
        return new RTMPHandshake2(epoch, timeReceived, random);
    }

}
