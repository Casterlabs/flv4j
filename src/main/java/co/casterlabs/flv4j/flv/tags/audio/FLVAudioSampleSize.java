package co.casterlabs.flv4j.flv.tags.audio;

import lombok.AllArgsConstructor;

//https://veovera.org/docs/legacy/video-file-format-v10-0-spec.pdf#page=10
@AllArgsConstructor
public enum FLVAudioSampleSize {
    BIT_8(0),
    BIT_16(1),
    ;

    public static final FLVAudioSampleSize[] LUT = new FLVAudioSampleSize[2];
    static {
        for (FLVAudioSampleSize e : values()) {
            LUT[e.id] = e;
        }
    }

    public final int id;

}
