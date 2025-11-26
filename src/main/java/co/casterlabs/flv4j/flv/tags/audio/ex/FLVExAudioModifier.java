package co.casterlabs.flv4j.flv.tags.audio.ex;

import co.casterlabs.flv4j.actionscript.io.ASAssert;

public record FLVExAudioModifier(int rawType, byte[] data) {

    public FLVExAudioModifier(int rawType, byte[] data) {
        ASAssert.u4(rawType, "rawType");
        assert data != null : "data cannot be null";
        this.rawType = rawType;
        this.data = data;
    }

    public FLVExAudioModifier(FLVExAudioModifierType type, byte[] data) {
        this(type.id, data);
    }

    public FLVExAudioModifierType type() {
        return FLVExAudioModifierType.LUT[this.rawType];
    }

    @Override
    public final String toString() {
        return String.format(
            "FLVExAudioModifier[type=%s (%d)]",
            this.type(), this.rawType
        );
    }

}
