package co.casterlabs.flv4j.actionscript.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import co.casterlabs.commons.io.marshalling.PrimitiveMarshall;

// https://rtmp.veriskope.com/pdf/amf0-file-format-specification.pdf#page=2
public record ASWriter(
    OutputStream out
) {
    private static final PrimitiveMarshall M = PrimitiveMarshall.BIG_ENDIAN;

    public void bytes(byte[] b) throws IOException {
        out.write(b);
    }

    public void bytes(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    public void u8(int value) throws IOException {
        out.write(value);
    }

    public void u16(int value) throws IOException {
        out.write(
            M.intToBytes(value),
            2,
            2
        );
    }

    public void s16(short value) throws IOException {
        out.write(M.shortToBytes(value));
    }

    public void u24(int value) throws IOException {
        out.write(
            M.intToBytes(value),
            1,
            3
        );
    }

    public void u32(long value) throws IOException {
        out.write(
            M.longToBytes(value),
            4,
            4
        );
    }

    public void dbl(double value) throws IOException {
        out.write(M.doubleToBytes(value));
    }

    public void utf8(String str) throws IOException {
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);

        u16(strBytes.length);
        out.write(strBytes);
    }

    public void utf8long(String str) throws IOException {
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);

        u32(strBytes.length);
        out.write(strBytes);
    }

    public void utf8empty() throws IOException {
        u8(0); // Write a u16 value of 0.
        u8(0);
    }

}
