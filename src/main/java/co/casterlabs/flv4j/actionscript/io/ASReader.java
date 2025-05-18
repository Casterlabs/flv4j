package co.casterlabs.flv4j.actionscript.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import co.casterlabs.commons.io.streams.LimitedInputStream;
import co.casterlabs.flv4j.util.EndOfStreamException;

// https://rtmp.veriskope.com/pdf/amf0-file-format-specification.pdf#page=2
// https://rtmp.veriskope.com/pdf/amf3-file-format-spec.pdf#page=3 for u29
public record ASReader(
    InputStream in
) {

    public ASReader(byte[] b) {
        this(new ByteArrayInputStream(b));
    }

    public ASReader(byte[] b, int off, int len) {
        this(new ByteArrayInputStream(b, off, len));
    }

    /* -------------------- */
    /* -------------------- */
    /* -------------------- */

    public byte[] bytes(int len) throws IOException {
        byte[] buf = new byte[len];
        int total = 0;
        while (total < len) {
            int read = in.read(buf, total, len - total);
            if (read == -1) throw new EndOfStreamException("End of stream");
            total += read;
        }
        return buf;
    }

    public int u8() throws IOException {
        int read = in.read();
        if (read == -1) throw new EndOfStreamException("End of stream");
        return read;
    }

    public int u16() throws IOException {
        return u8() << 8 | u8();
    }

    public short s16() throws IOException {
        return (short) (u8() << 8 | u8());
    }

    public int u24() throws IOException {
        return u8() << 16 | u8() << 8 | u8();
    }

    public long u32() throws IOException {
        return u8() << 24 | u8() << 16 | u8() << 8 | u8();
    }

    public double dbl() throws IOException {
        long bits = (long) u8() << 56
            | (long) u8() << 48
            | (long) u8() << 40
            | (long) u8() << 32
            | (long) u8() << 24
            | (long) u8() << 16
            | (long) u8() << 8
            | (long) u8() << 0;
        return Double.longBitsToDouble(bits);
    }

    public String utf8() throws IOException {
        int len = u16();
        byte[] bytes = bytes(len);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public String utf8long() throws IOException {
        int len = (int) u32();
        byte[] bytes = bytes(len);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public ASReader limited(int len) {
        return new ASReader(new LimitedInputStream(this.in, len));
    }

}
