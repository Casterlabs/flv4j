package co.casterlabs.flv4j.flv.packets.payload;

import co.casterlabs.flv4j.FLVSerializable;

public interface FLVPayload extends FLVSerializable {

    public boolean isSequenceHeader();

}
