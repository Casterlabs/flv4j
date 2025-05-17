package co.casterlabs.flv4j.packets.payload;

import co.casterlabs.flv4j.FLVRawSerializable;

public record FLVUnknownPayload(
    byte[] raw
) implements FLVPayload, FLVRawSerializable {

    @Override
    public boolean isSequenceHeader() {
        return false;
    }

    @Override
    public final String toString() {
        return String.format(
            "FLVUnknownPayload[isSequenceHeader=?, size=%d]",
            this.size()
        );
    }

}
