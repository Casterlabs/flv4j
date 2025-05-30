package co.casterlabs.flv4j.actionscript.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import co.casterlabs.commons.io.streams.LimitedInputStream;
import co.casterlabs.flv4j.EndOfStreamException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

// https://rtmp.veriskope.com/pdf/amf0-file-format-specification.pdf#page=2
// https://rtmp.veriskope.com/pdf/amf3-file-format-spec.pdf#page=3 for u29
@Accessors(fluent = true)
@RequiredArgsConstructor
public class ASReader {
    private final InputStream in;
    private @Getter int bytesRead = 0;

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
        this.bytesRead += len;
        return buf;
    }

    public int u8() throws IOException {
        int read = in.read();
        this.bytesRead++;
        if (read == -1) throw new EndOfStreamException("End of stream");
        return read;
    }

    public int u16() throws IOException {
        return u8() << 8 | u8();
    }

    public short s16() throws IOException {
        return (short) u16();
    }

    public int u24() throws IOException {
        return u8() << 16 | u8() << 8 | u8();
    }

    public int u29() throws IOException {
        int b1 = u8();
        int result = b1 & 0x7F;
        if ((b1 & 0x80) == 0) {
            return result;
        }

        int b2 = u8();
        result = (result << 7) | (b2 & 0x7F);
        if ((b2 & 0x80) == 0) {
            return result;
        }

        int b3 = u8();
        result = (result << 7) | (b3 & 0x7F);
        if ((b3 & 0x80) == 0) {
            return result;
        }

        // NB: All 8 bits are used in the final byte, so we DO NOT shift by 7.
        int b4 = u8();
        return (result << 8) | b4;
    }

    public long u32() throws IOException {
        return (long) u8() << 24
            | (long) u8() << 16
            | (long) u8() << 8
            | (long) u8() << 0;
    }

    public long u32le() throws IOException {
        return (long) u8() << 0
            | (long) u8() << 8
            | (long) u8() << 16
            | (long) u8() << 24;
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
        this.bytesRead += len;
        return new ASReader(new LimitedInputStream(this.in, len));
    }

}
