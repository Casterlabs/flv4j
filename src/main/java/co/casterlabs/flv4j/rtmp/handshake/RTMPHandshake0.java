package co.casterlabs.flv4j.rtmp.handshake;

import java.io.IOException;

import co.casterlabs.flv4j.FLVSerializable;
import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASSizer;
import co.casterlabs.flv4j.actionscript.io.ASWriter;

// https://veovera.org/docs/legacy/rtmp-v1-0-spec.pdf#page=7
public record RTMPHandshake0(
    int version
) implements FLVSerializable {

    @Override
    public int size() {
        return ASSizer.u8; // version
    }

    @Override
    public void serialize(ASWriter writer) throws IOException {
        writer.u8(this.version);
    }

    public static RTMPHandshake0 parse(ASReader reader) throws IOException {
        int version = reader.u8();
        return new RTMPHandshake0(version);
    }

}
