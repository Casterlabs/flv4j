package co.casterlabs.flv4j.flv.tags.video.avc;

import lombok.AllArgsConstructor;

// https://www.itu.int/rec/T-REC-H.264-202408-I/en page 91
@AllArgsConstructor
public enum AVCNaluType {
    UNSPECIFIED(0),
    CODED_SLICE_NON_IDR_PICTURE(1),
    CODED_SLICE_DATA_PART_A(2),
    CODED_SLICE_DATA_PART_B(3),
    CODED_SLICE_DATA_PART_C(4),
    CODED_SLICE_IDR_PICTURE(5),
    SEI_PARAMETERS(6),
    SPS(7),
    PPS(8),
    AUD(9),
    END_OF_SEQUENCE(10),
    END_OF_STREAM(11),
    FILLER_DATA(12),
    SPS_EXT(13),
    PREFIX_NAL_UNIT(14),
    SUBSET_SPS(15),
    DEPTH_PARAMETER_SET(16),
    // 17-18 are reserved
    CODED_SLICE_AUXILIARY_PICTURE(19),
    CODED_SLICE_EXTENSION(20),
    CODED_SLICE_EXTENSION_DEPTH_VIEW(21)
    // 22-23 are reserved
    // 24-31 are unspecified
    ;

    public static final AVCNaluType[] LUT = new AVCNaluType[32];
    static {
        for (AVCNaluType e : values()) {
            LUT[e.id] = e;
        }
    }

    public final int id;

}
