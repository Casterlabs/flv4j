package co.casterlabs.flv4j.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import co.casterlabs.commons.io.marshalling.PrimitiveMarshall;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

// https://rtmp.veriskope.com/pdf/amf0-file-format-specification.pdf#page=2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ASWriter {
    private static final PrimitiveMarshall M = PrimitiveMarshall.BIG_ENDIAN;

    public static void u8(OutputStream out, int value) throws IOException {
        out.write(value);
    }

    public static void u16(OutputStream out, int value) throws IOException {
        out.write(
            M.intToBytes(value),
            2,
            2
        );
    }

    public static void s16(OutputStream out, short value) throws IOException {
        out.write(M.shortToBytes(value));
    }

    public static void u24(OutputStream out, int value) throws IOException {
        out.write(
            M.intToBytes(value),
            1,
            3
        );
    }

    public static void u32(OutputStream out, long value) throws IOException {
        out.write(
            M.longToBytes(value),
            4,
            4
        );
    }

    public static void dbl(OutputStream out, double value) throws IOException {
        out.write(M.doubleToBytes(value));
    }

    public static void utf8(OutputStream out, String str) throws IOException {
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);

        u16(out, strBytes.length);
        out.write(strBytes);
    }

    public static void utf8long(OutputStream out, String str) throws IOException {
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);

        u32(out, strBytes.length);
        out.write(strBytes);
    }

    public static void utf8empty(OutputStream out) throws IOException {
        u8(out, 0); // Write a u16 value of 0.
        u8(out, 0);
    }

}
