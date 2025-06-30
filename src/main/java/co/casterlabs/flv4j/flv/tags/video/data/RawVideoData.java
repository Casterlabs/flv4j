package co.casterlabs.flv4j.flv.tags.video.data;

import co.casterlabs.flv4j.FLVRawSerializable;

public record RawVideoData(
    byte[] raw
) implements VideoData, FLVRawSerializable {

    @Override
    public boolean isSequenceHeader() {
        return false;
    }

    @Override
    public int compositionTimeOffset() {
        return 0;
    }

    @Override
    public final String toString() {
        return String.format(
            "RawVideoData[size=%d]",
            this.size()
        );
    }

}
