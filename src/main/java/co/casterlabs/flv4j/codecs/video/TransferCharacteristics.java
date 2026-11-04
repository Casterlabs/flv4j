package co.casterlabs.flv4j.codecs.video;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TransferCharacteristics {
    BT709("BT.709"),
    BT470M("BT.470 System M"),
    BT470BG("BT.470 System B/G"),
    SMPTE170M("SMPTE 170M"),
    SMPTE240M("SMPTE 240M"),
    LINEAR("linear"),
    LOG100("logarithmic (100:1)"),
    LOG100_SQRT10("logarithmic (100√10:1)"),
    IEC61966_2_4("IEC 61966-2-4"), // xvYCC
    BT1361("BT.1361"),
    SRGB("sRGB"),
    BT2020_10("BT.2020 10-bit"),
    BT2020_12("BT.2020 12-bit"),
    SMPTE2084("SMPTE ST 2084"), // PQ
    SMPTE428("SMPTE ST 428"),
    HLG("hybrid log-gamma"),

    UNSPECIFIED("unspecified"),
    RESERVED("reserved"),
    UNKNOWN("unknown");

    public final String id;

    // https://www.itu.int/rec/T-REC-H.273-202407-I/en §8.2
    public static TransferCharacteristics fromH273(int value) {
        return switch (value) {
            case 1 -> BT709;
            case 4 -> BT470M;
            case 5 -> BT470BG;
            case 6 -> SMPTE170M;
            case 7 -> SMPTE240M;
            case 8 -> LINEAR;
            case 9 -> LOG100;
            case 10 -> LOG100_SQRT10;
            case 11 -> IEC61966_2_4;
            case 12 -> BT1361;
            case 13 -> SRGB;
            case 14 -> BT2020_10;
            case 15 -> BT2020_12;
            case 16 -> SMPTE2084;
            case 17 -> SMPTE428;
            case 18 -> HLG;
            case 2 -> UNSPECIFIED;
            case 0, 3 -> RESERVED;
            default -> UNKNOWN;
        };
    }

}
