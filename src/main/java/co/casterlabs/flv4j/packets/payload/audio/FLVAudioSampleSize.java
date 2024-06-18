package co.casterlabs.flv4j.packets.payload.audio;

import lombok.AllArgsConstructor;

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
