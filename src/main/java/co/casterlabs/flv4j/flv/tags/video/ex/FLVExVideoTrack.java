package co.casterlabs.flv4j.flv.tags.video.ex;

import co.casterlabs.flv4j.FourCC;
import co.casterlabs.flv4j.actionscript.io.ASAssert;

// https://veovera.org/docs/enhanced/enhanced-rtmp-v2#enhanced-video
public record FLVExVideoTrack(
    FourCC codec,
    int id,
    FLVExVideoCodecData data
) {

    public FLVExVideoTrack(FourCC codec, int id, FLVExVideoCodecData data) {
        ASAssert.u8(id, "id");
        assert codec != null : "codec cannot be null";
        assert data != null : "data cannot be null";

        this.codec = codec;
        this.id = id;
        this.data = data;
    }

    @Override
    public final String toString() {
        return String.format(
            "FLVExVideoTrack[codec=%s, id=%d, data=%s]",
            this.codec, this.id, this.data
        );
    }

}
