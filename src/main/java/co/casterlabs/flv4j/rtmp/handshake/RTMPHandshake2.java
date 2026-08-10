package co.casterlabs.flv4j.rtmp.handshake;

import java.io.IOException;

import co.casterlabs.flv4j.FLVSerializable;
import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASSizer;
import co.casterlabs.flv4j.actionscript.io.ASWriter;

// https://veovera.org/docs/legacy/rtmp-v1-0-spec.pdf#page=9
public record RTMPHandshake2(
    long epoch,
    long timeReceived,
    byte[] randomEcho
) implements FLVSerializable {

    @Override
    public int size() {
        return ASSizer.u32 // epoch
            + ASSizer.u32  // time received
            + this.randomEcho.length;
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
