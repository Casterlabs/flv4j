package co.casterlabs.flv4j.flv.packets.payload.video;

import lombok.AllArgsConstructor;

//https://rtmp.veriskope.com/pdf/video_file_format_spec_v10.pdf#page=13
@AllArgsConstructor
public enum FLVVideoCodec {
    JPEG(1),

    SORENSON_H263(2),

    SCREEN(3),
    SCREEN_2(6),

    ON2_VP6(4),
    ON2_VP6_ALPHA(5),

    H264(7),
    ;

    public static final FLVVideoCodec[] LUT = new FLVVideoCodec[16];
    static {
        for (FLVVideoCodec e : values()) {
            LUT[e.id] = e;
        }
    }

    public final int id;

}
