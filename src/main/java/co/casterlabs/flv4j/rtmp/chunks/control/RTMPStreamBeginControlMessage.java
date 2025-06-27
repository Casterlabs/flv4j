package co.casterlabs.flv4j.rtmp.chunks.control;

import java.io.IOException;

import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASWriter;

// https://rtmp.veriskope.com/docs/spec/#717user-control-message-events
public record RTMPStreamBeginControlMessage(long streamId) implements RTMPControlMessageStream {

    @Override
    public int type() {
        return 0;
    }

    @Override
    public int size() {
        return 4;
    }

    @Override
    public void serialize(ASWriter writer) throws IOException {
        writer.u32(this.streamId);
    }

    public static RTMPStreamBeginControlMessage parse(ASReader reader, int length) throws IOException {
        long streamId = reader.u32();
        return new RTMPStreamBeginControlMessage(streamId);
    }

}
