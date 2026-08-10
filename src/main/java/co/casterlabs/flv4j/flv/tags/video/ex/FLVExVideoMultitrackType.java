package co.casterlabs.flv4j.flv.tags.video.ex;

import lombok.AllArgsConstructor;

//https://veovera.org/docs/enhanced/enhanced-rtmp-v2#enhanced-video
@AllArgsConstructor
public enum FLVExVideoMultitrackType {
    ONE_TRACK(0),
    MANY_TRACKS(1),
    MANY_TRACKS_MANY_CODECS(2),
    ;

    public static final FLVExVideoMultitrackType[] LUT = new FLVExVideoMultitrackType[16];

    static {
        for (FLVExVideoMultitrackType e : values()) {
            LUT[e.id] = e;
        }
    }

    public final int id;

}
