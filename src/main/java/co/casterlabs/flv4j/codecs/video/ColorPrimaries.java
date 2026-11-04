package co.casterlabs.flv4j.codecs.video;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ColorPrimaries {
    BT709("BT.709"), // BT.709 / sRGB
    BT470M("BT.470 System M"),
    BT470BG("BT.470 System B/G"),
    SMPTE170M("SMPTE 170M"), // BT.601 525
    SMPTE240M("SMPTE 240M"),
    GENERIC_FILM("generic film"),
    BT2020("BT.2020"),
    SMPTE428("SMPTE 428"), // CIE XYZ
    SMPTE431("SMPTE 431"), // DCI-P3
    SMPTE432("SMPTE 432"), // Display P3
    EBU3213("EBU Tech. 3213-E"),

    UNSPECIFIED("unspecified"),
    RESERVED("reserved"),
    UNKNOWN("unknown");

    public final String id;

    // https://www.itu.int/rec/T-REC-H.273-202407-I/en §8.1
    public static ColorPrimaries fromH273(int value) {
        return switch (value) {
            case 1 -> BT709;
            case 4 -> BT470M;
            case 5 -> BT470BG;
            case 6 -> SMPTE170M;
            case 7 -> SMPTE240M;
            case 8 -> GENERIC_FILM;
            case 9 -> BT2020;
            case 10 -> SMPTE428;
            case 11 -> SMPTE431;
            case 12 -> SMPTE432;
            case 22 -> EBU3213;
            case 2 -> UNSPECIFIED;
            case 0, 3, 13, 14, 15, 16, 17, 18, 19, 20, 21 -> RESERVED;
            default -> UNKNOWN;
        };
    }

}
