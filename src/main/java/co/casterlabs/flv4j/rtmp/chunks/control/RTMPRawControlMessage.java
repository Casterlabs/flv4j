package co.casterlabs.flv4j.rtmp.chunks.control;

import java.io.IOException;

import co.casterlabs.flv4j.actionscript.io.ASWriter;

// https://rtmp.veriskope.com/docs/spec/#717user-control-message-events
public record RTMPRawControlMessage(byte[] raw) implements RTMPControlMessage {

    @Override
    public int size() {
        return this.raw.length;
    }

    @Override
    public void serialize(ASWriter writer) throws IOException {
        writer.bytes(this.raw);
    }

}
