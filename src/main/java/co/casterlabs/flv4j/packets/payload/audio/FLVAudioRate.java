package co.casterlabs.flv4j.packets.payload.audio;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum FLVAudioRate {
    KHZ_5_5(0),
    KHZ_11(1),
    KHZ_22(2),
    KHZ_33(3),
    ;

    public static final FLVAudioRate[] LUT = new FLVAudioRate[4];
    static {
        for (FLVAudioRate e : values()) {
            LUT[e.id] = e;
        }
    }

    public final int id;

}
