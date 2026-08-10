package co.casterlabs.flv4j.flv.tags.audio;

import lombok.AllArgsConstructor;

//https://veovera.org/docs/legacy/video-file-format-v10-0-spec.pdf#page=11
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
