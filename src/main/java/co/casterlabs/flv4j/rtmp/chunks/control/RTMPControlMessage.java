package co.casterlabs.flv4j.rtmp.chunks.control;

import co.casterlabs.flv4j.FLVSerializable;

// https://veovera.org/docs/legacy/rtmp-v1-0-spec.pdf#page=27
public interface RTMPControlMessage extends FLVSerializable {

    public int type();

}
