package co.casterlabs.flv4j.codecs.video.hvc1;

import lombok.AllArgsConstructor;

// https://www.itu.int/rec/T-REC-H.265-202601-I/en §7.4.2.2
@AllArgsConstructor
public enum HEVCNaluType {
    TRAIL_N(0),
    TRAIL_R(1),
    TSA_N(2),
    TSA_R(3),
    STSA_N(4),
    STSA_R(5),
    RADL_N(6),
    RADL_R(7),
    RASL_N(8),
    RASL_R(9),
    // 10-23 are reserved for VCL
    BLA_W_LP(16),
    BLA_W_RADL(17),
    BLA_N_LP(18),
    IDR_W_RADL(19),
    IDR_N_LP(20),
    CRA_NUT(21),
    // 22-23 are reserved for IRAP VCL
    // 24-31 are reserved for complementary VCL NAL units
    VPS(32),
    SPS(33),
    PPS(34),
    AUD(35),
    END_OF_SEQUENCE(36),
    END_OF_BITSTREAM(37),
    FILLER_DATA(38),
    PREFIX_SEI(39),
    SUFFIX_SEI(40)
    // 41-63 are reserved / unspecified
    ;

    public static final HEVCNaluType[] LUT = new HEVCNaluType[64];
    static {
        for (HEVCNaluType e : values()) {
            LUT[e.id] = e;
        }
    }

    public final int id;

}
