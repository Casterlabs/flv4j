package co.casterlabs.flv4j.actionscript.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

// https://rtmp.veriskope.com/pdf/amf0-file-format-specification.pdf#page=2
public record ASWriter(
    OutputStream out
) {

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
        out.write(value >> 8 & 0xFF);
        out.write(value & 0xFF);
    }

    public void s16(short value) throws IOException {
        out.write(value >> 8 & 0xFF);
        out.write(value & 0xFF);
    }

    public void u24(int value) throws IOException {
        out.write(value >> 16 & 0xFF);
        out.write(value >> 8 & 0xFF);
        out.write(value & 0xFF);
    }

    public void u32(long value) throws IOException {
        u8((int) value >> 24 & 0xFF);
        u8((int) value >> 16 & 0xFF);
        u8((int) value >> 8 & 0xFF);
        u8((int) value & 0xFF);
    }

    public void dbl(double value) throws IOException {
        long bits = Double.doubleToRawLongBits(value);

        u8((int) (bits >> 56 & 0xFF));
        u8((int) (bits >> 48 & 0xFF));
        u8((int) (bits >> 40 & 0xFF));
        u8((int) (bits >> 32 & 0xFF));
        u8((int) (bits >> 24 & 0xFF));
        u8((int) (bits >> 16 & 0xFF));
        u8((int) (bits >> 8 & 0xFF));
        u8((int) (bits >> 0 & 0xFF));
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
