package co.casterlabs.flv4j.packets.payload.audio;

import lombok.AllArgsConstructor;

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
