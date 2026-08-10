package co.casterlabs.flv4j.flv.tags.video.ex;

import co.casterlabs.flv4j.FourCC;
import co.casterlabs.flv4j.actionscript.io.ASAssert;

// https://veovera.org/docs/enhanced/enhanced-rtmp-v2#enhanced-audio
public record FLVExVideoTrack(
    FourCC codec,
    int id,
    int compositionTimeOffset,
    FLVExVideoCodecData data
) {

    public FLVExVideoTrack(FourCC codec, int id, int compositionTimeOffset, FLVExVideoCodecData data) {
        ASAssert.u8(id, "id");
        ASAssert.s24(compositionTimeOffset, "compositionTimeOffset");
        assert codec != null : "codec cannot be null";
        assert data != null : "data cannot be null";

        this.codec = codec;
        this.id = id;
        this.compositionTimeOffset = compositionTimeOffset;
        this.data = data;
    }

    @Override
    public final String toString() {
        return String.format(
            "FLVExVideoTrack[codec=%s, id=%d, compositionTimeOffset=%d, data=%s]",
            this.codec, this.id, this.compositionTimeOffset, this.data
        );
    }

}
