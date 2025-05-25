package co.casterlabs.flv4j.flv.tags.audio;

import lombok.AllArgsConstructor;

//https://rtmp.veriskope.com/pdf/video_file_format_spec_v10.pdf#page=10
@AllArgsConstructor
public enum FLVAudioRate {
    KHZ_5_5(0),
    KHZ_11(1),
    KHZ_22(2),
    KHZ_44(3),
    ;

    public static final FLVAudioRate[] LUT = new FLVAudioRate[4];
    static {
        for (FLVAudioRate e : values()) {
            LUT[e.id] = e;
        }
    }

    public final int id;

}
