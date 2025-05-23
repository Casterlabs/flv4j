package co.casterlabs.flv4j.rtmp.chunks;

import co.casterlabs.flv4j.FLVRawSerializable;

public record RTMPMessageUnknown(int rawType, byte[] raw) implements RTMPMessage, FLVRawSerializable {

    @Override
    public final String toString() {
        return String.format("RTMPMessageUnknown[type=%d, length=%d]", this.rawType, this.raw.length);
    }

}
