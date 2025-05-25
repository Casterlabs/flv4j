package co.casterlabs.flv4j.flv.tags.audio;

import lombok.AllArgsConstructor;

// https://rtmp.veriskope.com/pdf/video_file_format_spec_v10_1.pdf#page=76
@AllArgsConstructor
public enum FLVAudioFormat {
    LPCM(0),
    ADPCM(1),
    MP3(2),
    LPCM_LE(3),

    NELLYMOSER_16_MONO(4),
    NELLYMOSER_8_MONO(5),
    NELLYMOSER(6),

    G711_ALAW(7),
    G711_MULAW(8),

    // 9 is used to signal eFLV.

    AAC(10),
    SPEEX(11),
    // 12, 13 = reserved
    MP3_8(14),

    DEVICE_SPECIFIC(15),
    ;

    public static final FLVAudioFormat[] LUT = new FLVAudioFormat[16];
    static {
        for (FLVAudioFormat e : values()) {
            LUT[e.id] = e;
        }
    }

    public final int id;

}
