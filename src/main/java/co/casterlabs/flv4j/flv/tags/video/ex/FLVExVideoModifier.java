package co.casterlabs.flv4j.flv.tags.video.ex;

import co.casterlabs.flv4j.actionscript.io.ASAssert;

//https://veovera.org/docs/enhanced/enhanced-rtmp-v2#enhanced-video
public record FLVExVideoModifier(
    int rawType,
    byte[] data
) {

    public FLVExVideoModifier(int rawType, byte[] data) {
        ASAssert.u4(rawType, "rawType");
        assert data != null : "data cannot be null";

        this.rawType = rawType;
        this.data = data;
    }

}
