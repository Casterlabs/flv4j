package co.casterlabs.flv4j.flv.tags.video;

import lombok.AllArgsConstructor;

//https://veovera.org/docs/legacy/video-file-format-v10-0-spec.pdf#page=13
@AllArgsConstructor
public enum FLVVideoCodec {
    JPEG(1),

    SORENSON_H263(2),

    SCREEN(3),
    SCREEN_2(6),

    ON2_VP6(4),
    ON2_VP6_ALPHA(5),

    H264(7),

    // Non-standard codecs that have been found in the wild.
    NS_REALH263(8),
    NS_MPEG4(9),
    NS_HEVC(12),
    ;

    public static final FLVVideoCodec[] LUT = new FLVVideoCodec[16];
    static {
        for (FLVVideoCodec e : values()) {
            LUT[e.id] = e;
        }
    }

    public final int id;

}
