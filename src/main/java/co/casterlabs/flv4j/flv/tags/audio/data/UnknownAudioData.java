package co.casterlabs.flv4j.flv.tags.audio.data;

import co.casterlabs.flv4j.FLVRawSerializable;

public record UnknownAudioData(
    byte[] raw
) implements AudioData, FLVRawSerializable {

    @Override
    public final String toString() {
        return String.format(
            "UnknownAudioData[size=%d]",
            this.size()
        );
    }

}
