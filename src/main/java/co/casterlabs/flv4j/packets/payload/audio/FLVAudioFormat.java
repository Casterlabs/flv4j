package co.casterlabs.flv4j.packets.payload.audio;

import lombok.AllArgsConstructor;

//https://rtmp.veriskope.com/pdf/video_file_format_spec_v10.pdf#page=10
@AllArgsConstructor
public enum FLVAudioFormat {
    LPCM(0),
    ADPCM(1),
    LPCM_LE(3),

    MP3(2),
    MP3_8(14),
    AAC(10),
    SPEEX(11),

    NELLYMOSER_16_MONO(4),
    NELLYMOSER_8_MONO(5),
    NELLYMOSER(6),

    G711_ALAW(7),
    G711_MULAW(8),

    CUSTOM(15),
    ;

    public static final FLVAudioFormat[] LUT = new FLVAudioFormat[16];
    static {
        for (FLVAudioFormat e : values()) {
            LUT[e.id] = e;
        }
    }

    public final int id;

}
