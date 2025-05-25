package co.casterlabs.flv4j.flv.tags.audio.data;

import co.casterlabs.flv4j.FLVRawSerializable;

public record AudioData(
    byte[] raw
) implements FLVRawSerializable {

    @Override
    public final String toString() {
        return String.format(
            "AudioData[size=%d]",
            this.size()
        );
    }

}
