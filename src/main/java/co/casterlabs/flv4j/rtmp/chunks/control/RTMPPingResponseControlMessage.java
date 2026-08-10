package co.casterlabs.flv4j.rtmp.chunks.control;

import java.io.IOException;

import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASWriter;

// https://veovera.org/docs/legacy/rtmp-v1-0-spec.pdf#page=27
public record RTMPPingResponseControlMessage(long timestamp) implements RTMPControlMessage {

    @Override
    public int type() {
        return 7;
    }

    @Override
    public int size() {
        return 4;
    }

    @Override
    public void serialize(ASWriter writer) throws IOException {
        writer.u32(this.timestamp);
    }

    public static RTMPPingResponseControlMessage parse(ASReader reader, int length) throws IOException {
        long streamId = reader.u32();
        return new RTMPPingResponseControlMessage(streamId);
    }

}
