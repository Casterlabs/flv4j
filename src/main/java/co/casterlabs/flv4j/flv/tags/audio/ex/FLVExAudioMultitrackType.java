package co.casterlabs.flv4j.flv.tags.audio.ex;

import lombok.AllArgsConstructor;

// https://veovera.org/docs/enhanced/enhanced-rtmp-v2#enhanced-audio
@AllArgsConstructor
public enum FLVExAudioMultitrackType {
    ONE_TRACK(0),
    MANY_TRACKS(1),
    MANY_TRACKS_MANY_CODECS(2),
    ;

    public static final FLVExAudioMultitrackType[] LUT = new FLVExAudioMultitrackType[16];
    static {
        for (FLVExAudioMultitrackType e : values()) {
            LUT[e.id] = e;
        }
    }

    public final int id;

}
