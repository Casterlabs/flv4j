package co.casterlabs.flv4j.packets.payload.video.data;

import co.casterlabs.flv4j.FLVRawSerializable;

public record UnknownVideoData(
    byte[] raw
) implements VideoData, FLVRawSerializable {

    @Override
    public final String toString() {
        return String.format(
            "UnknownVideoData[size=%d]",
            this.size()
        );
    }

}
