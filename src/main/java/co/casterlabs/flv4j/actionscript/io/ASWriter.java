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
        ASAssert.u8(value, "value");
        out.write(value);
    }

    public void u16(int value) throws IOException {
        ASAssert.u16(value, "value");
        out.write(value >> 8 & 0xFF);
        out.write(value & 0xFF);
    }

    public void s16(short value) throws IOException {
        u16(value & 0xFFFF);
    }

    public void u24(int value) throws IOException {
        ASAssert.u24(value, "value");
        out.write(value >> 16 & 0xFF);
        out.write(value >> 8 & 0xFF);
        out.write(value & 0xFF);
    }

    public void u29(int value) throws IOException {
        ASAssert.u29(value, "value");
        // We could use a loop here, but we might as well unroll it.

        // Single byte: (0-127) (inclusive)
        if (value < 128) {
            u8(value);
            return;
        }

        // Two bytes: 128-16383 (inclusive)
        if (value < 16384) {
            u8(value >> 7 & 0x7F | 0x80);
            u8(value & 0x7F);
            return;
        }

        // Three bytes: 16384-2097151 (inclusive)
        if (value < 2097152) {
            u8(value >> 14 & 0x7F | 0x80);
            u8(value >> 7 & 0x7F | 0x80);
            u8(value & 0x7F);
            return;
        }

        // Four bytes: 2097152-536870911 (inclusive)
        u8(value >> 22 & 0x7F | 0x80);
        u8(value >> 15 & 0x7F | 0x80);
        u8(value >> 8 & 0x7F | 0x80); // NB: All 8 bits are used in the final byte, so we DO NOT shift by 7 here.
        u8(value & 0xFF);
    }

    public void u32(long value) throws IOException {
        ASAssert.u32(value, "value");
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
