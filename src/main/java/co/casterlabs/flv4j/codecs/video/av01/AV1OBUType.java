package co.casterlabs.flv4j.codecs.video.av01;

import lombok.AllArgsConstructor;

// https://aomediacodec.github.io/av1-spec/#obu-header-semantics
@AllArgsConstructor
public enum AV1OBUType {
    // 0 reserved
    SEQUENCE_HEADER(1),
    TEMPORAL_DELIMITER(2),
    FRAME_HEADER(3),
    TILE_GROUP(4),
    METADATA(5),
    FRAME(6),
    REDUNDANT_FRAME_HEADER(7),
    TILE_LIST(8),
    // 9-14 reserved
    PADDING(15),
    ;

    public static final AV1OBUType[] LUT = new AV1OBUType[16];
    static {
        for (AV1OBUType e : values()) {
            LUT[e.id] = e;
        }
    }

    public final int id;

}
