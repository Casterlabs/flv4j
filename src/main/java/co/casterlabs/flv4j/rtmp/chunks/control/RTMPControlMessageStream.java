package co.casterlabs.flv4j.rtmp.chunks.control;

// https://rtmp.veriskope.com/docs/spec/#717user-control-message-events
public interface RTMPControlMessageStream extends RTMPControlMessage {

    public long streamId();

}
