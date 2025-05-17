package co.casterlabs.flv4j.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import co.casterlabs.commons.io.marshalling.PrimitiveMarshall;
import co.casterlabs.commons.io.streams.LimitedInputStream;

// https://rtmp.veriskope.com/pdf/amf0-file-format-specification.pdf#page=2
public record ASReader(
    InputStream in
) {

    public ASReader(byte[] b, int off, int len) {
        this(new ByteArrayInputStream(b, off, len));
    }

    public byte[] bytes(int len) throws IOException {
        return in.readNBytes(len);
    }

    public int u8() throws IOException {
        int read = in.read();
        if (read == -1) throw new EndOfStreamException("End of stream");
        return read & 0xFF;
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
        byte[] bytes = bytes(Double.BYTES);
        return PrimitiveMarshall.BIG_ENDIAN.bytesToDouble(bytes);
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
