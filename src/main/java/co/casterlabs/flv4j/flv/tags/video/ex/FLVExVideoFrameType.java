package co.casterlabs.flv4j.flv.tags.video.ex;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum FLVExVideoFrameType {
    KEY_FRAME(1),
    INTER_FRAME(2),
    DISPOSABLE_INTER_FRAME(3),
    GENERATED_KEY_FRAME(4),
    COMMAND(5),
    ;

    public static final FLVExVideoFrameType[] LUT = new FLVExVideoFrameType[8];

    static {
        for (FLVExVideoFrameType e : values()) {
            LUT[e.id] = e;
        }
    }

    public final int id;

}
