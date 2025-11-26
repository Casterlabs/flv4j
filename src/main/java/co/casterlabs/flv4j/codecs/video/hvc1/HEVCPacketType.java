package co.casterlabs.flv4j.codecs.video.hvc1;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum HEVCPacketType {
    SEQUENCE_HEADER(0),
    NALU(1),
    END_OF_SEQUENCE(2),
    ;

    public static final HEVCPacketType[] LUT = new HEVCPacketType[255];
    static {
        for (HEVCPacketType e : values()) {
            LUT[e.id] = e;
        }
    }

    public final int id;

}