package co.casterlabs.flv4j.packets.payload.video;

import lombok.AllArgsConstructor;

//https://rtmp.veriskope.com/pdf/video_file_format_spec_v10.pdf#page=13
@AllArgsConstructor
public enum FLVVideoFrameType {
    KEY_FRAME(1),
    GENERATED_KEY_FRAME(4),

    INTER_FRAME(2),
    DISPOSABLE_INTER_FRAME(3),

    COMMAND_FRAME(5),
    ;

    public static final FLVVideoFrameType[] LUT = new FLVVideoFrameType[16];
    static {
        for (FLVVideoFrameType e : values()) {
            LUT[e.id] = e;
        }
    }

    public final int id;

}
