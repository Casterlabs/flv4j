package co.casterlabs.flv4j.flv.tags.video.data;

import co.casterlabs.flv4j.FLVRawSerializable;

public record VideoData(
    byte[] raw
) implements FLVRawSerializable {

    @Override
    public final String toString() {
        return String.format(
            "VideoData[size=%d]",
            this.size()
        );
    }

}
