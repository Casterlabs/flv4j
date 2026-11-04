package co.casterlabs.flv4j.codecs.audio.aac;

import lombok.AllArgsConstructor;

// https://veovera.org/docs/legacy/video-file-format-v10-0-spec.pdf#page=12
@AllArgsConstructor
public enum AACPacketType {
    SEQUENCE_HEADER(0),
    RAW(1),
    ;

    public static final AACPacketType[] LUT = new AACPacketType[255];
    static {
        for (AACPacketType e : values()) {
            LUT[e.id] = e;
        }
    }

    public final int id;

}
