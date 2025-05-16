package co.casterlabs.flv4j.packets.payload.audio;

import lombok.AllArgsConstructor;

//https://rtmp.veriskope.com/pdf/video_file_format_spec_v10.pdf#page=11
@AllArgsConstructor
public enum FLVAudioChannels {
    MONO(0),
    STEREO(1),
    ;

    public static final FLVAudioChannels[] LUT = new FLVAudioChannels[2];
    static {
        for (FLVAudioChannels e : values()) {
            LUT[e.id] = e;
        }
    }

    public final int id;

}
