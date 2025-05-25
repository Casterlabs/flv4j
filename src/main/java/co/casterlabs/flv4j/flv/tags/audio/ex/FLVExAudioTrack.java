package co.casterlabs.flv4j.flv.tags.audio.ex;

import co.casterlabs.flv4j.FourCC;
import co.casterlabs.flv4j.actionscript.io.ASAssert;
import co.casterlabs.flv4j.flv.tags.audio.data.AudioData;

// https://veovera.org/docs/enhanced/enhanced-rtmp-v2#enhanced-audio
public record FLVExAudioTrack(
    FourCC codec,
    int id,
    AudioData data
) {

    public FLVExAudioTrack(FourCC codec, int id, AudioData data) {
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
            "FLVExAudioTrack[codec=%s, id=%d, data=%s",
            this.codec, this.id, this.data
        );
    }

}
