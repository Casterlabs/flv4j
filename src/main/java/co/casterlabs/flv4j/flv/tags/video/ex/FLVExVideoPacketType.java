package co.casterlabs.flv4j.flv.tags.video.ex;

import lombok.AllArgsConstructor;

//https://veovera.org/docs/enhanced/enhanced-rtmp-v2#enhanced-video
@AllArgsConstructor
public enum FLVExVideoPacketType {
    SEQUENCE_START(0),
    CODED_FRAMES(1),
    SEQUENCE_END(2),
    CODED_FRAMES_X(3),
    METADATA(4),
    MPEG2TS_SEQUENCE_START(5),
    MULTITRACK(6),
    MOD_EX(7),
    ;

    public static final FLVExVideoPacketType[] LUT = new FLVExVideoPacketType[16];

    static {
        for (FLVExVideoPacketType e : values()) {
            LUT[e.id] = e;
        }
    }

    public final int id;

}
