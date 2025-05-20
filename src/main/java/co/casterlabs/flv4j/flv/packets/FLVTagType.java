package co.casterlabs.flv4j.flv.packets;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum FLVTagType {
    AUDIO(8),
    VIDEO(9),
    SCRIPT(18),
    ;

    public static final FLVTagType[] LUT = new FLVTagType[255];
    static {
        for (FLVTagType e : values()) {
            LUT[e.id] = e;
        }
    }

    public final int id;

}
