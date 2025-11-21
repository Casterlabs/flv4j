package co.casterlabs.flv4j.actionscript.io;

public class ASSizer {

    public static final int u8 = 1;
    public static final int u16 = 2;
    public static final int s16 = 2;
    public static final int u24 = 3;
    public static final int u32 = 4;
    public static final int dbl = 8;

    public static final int utf8empty = u16; // A u16 value of 0.

    public static int u29(int value) {
        // Single byte: (0-127) (inclusive)
        if (value < 128) {
            return 1;
        }

        // Two bytes: 128-16383 (inclusive)
        if (value < 16384) {
            return 2;
        }

        // Three bytes: 16384-2097151 (inclusive)
        if (value < 2097152) {
            return 3;
        }

        // Four bytes: 2097152-536870911 (inclusive)
        return 4;
    }

    public static int utf8(ByteString str) {
        return u16 + str.byteLength();
    }

    public static int utf8long(ByteString str) {
        return u32 + str.byteLength();
    }

}
