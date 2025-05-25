package co.casterlabs.flv4j.flv.tags;

import co.casterlabs.flv4j.FLVRawSerializable;
import co.casterlabs.flv4j.actionscript.io.ASAssert;

public record FLVTagHeaderUnknown(
    byte[] raw
) implements FLVTagData, FLVRawSerializable {

    public FLVTagHeaderUnknown(byte[] raw) {
        ASAssert.u24(raw.length, "raw.length");
        this.raw = raw;
    }

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
