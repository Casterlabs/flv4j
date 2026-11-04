package co.casterlabs.flv4j.codecs.video;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MatrixCoefficients {
    IDENTITY("identity"), // RGB
    BT709("BT.709"),
    FCC("FCC"),
    BT470BG("BT.470 System B/G"),
    SMPTE170M("SMPTE 170M"),
    SMPTE240M("SMPTE 240M"),
    YCGCO("YCgCo"),
    BT2020_NCL("BT.2020 non-constant luminance"),
    BT2020_CL("BT.2020 constant luminance"),
    SMPTE2085("SMPTE ST 2085"),
    CHROMAT_NCL("chromaticity-derived non-constant luminance"),
    CHROMAT_CL("chromaticity-derived constant luminance"),
    ICTCP("ICtCp"),
    IPT_C2("IPT-C2"),
    YCGCO_RE("YCgCo-Re"),
    YCGCO_RO("YCgCo-Ro"),

    UNSPECIFIED("unspecified"),
    RESERVED("reserved"),
    UNKNOWN("unknown");

    public final String id;

    // https://www.itu.int/rec/T-REC-H.273-202407-I/en §8.3
    public static MatrixCoefficients fromH273(int value) {
        return switch (value) {
            case 0 -> IDENTITY;
            case 1 -> BT709;
            case 4 -> FCC;
            case 5 -> BT470BG;
            case 6 -> SMPTE170M;
            case 7 -> SMPTE240M;
            case 8 -> YCGCO;
            case 9 -> BT2020_NCL;
            case 10 -> BT2020_CL;
            case 11 -> SMPTE2085;
            case 12 -> CHROMAT_NCL;
            case 13 -> CHROMAT_CL;
            case 14 -> ICTCP;
            case 15 -> IPT_C2;
            case 16 -> YCGCO_RE;
            case 17 -> YCGCO_RO;
            case 2 -> UNSPECIFIED;
            case 3 -> RESERVED;
            default -> UNKNOWN;
        };
    }

}
