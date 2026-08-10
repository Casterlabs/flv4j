package co.casterlabs.flv4j.rtmp.chunks.control;

// https://veovera.org/docs/legacy/rtmp-v1-0-spec.pdf#page=27
public interface RTMPControlMessageStream extends RTMPControlMessage {

    public long streamId();

}
