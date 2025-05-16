package co.casterlabs.flv4j.packets.payload;

import co.casterlabs.flv4j.FLVSerializable;

public interface FLVPayload extends FLVSerializable {

    public boolean isSequenceHeader();

}
