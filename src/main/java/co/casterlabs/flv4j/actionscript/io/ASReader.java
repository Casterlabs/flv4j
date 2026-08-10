package co.casterlabs.flv4j.actionscript.io;

import java.io.IOException;
import java.io.InputStream;

import co.casterlabs.commons.io.streams.LimitedInputStream;
import co.casterlabs.flv4j.EndOfStreamException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

// https://veovera.org/docs/legacy/amf0-file-format-spec.pdf#page=2
// https://veovera.org/docs/legacy/amf3-file-format-spec.pdf#page=3 for u29
public abstract class ASReader {

    public abstract ASReader limited(int len);

    public abstract int bytesRead();

    public abstract byte[] bytes(int len) throws IOException;

    public abstract int u8() throws IOException;

    public final int u16() throws IOException {
        return u8() << 8 | u8();
    }

    public final short s16() throws IOException {
        return (short) u16();
    }

    public final int u24() throws IOException {
        return u8() << 16 | u8() << 8 | u8();
    }

    public final int u29() throws IOException {
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

    public final long u32() throws IOException {
        return (long) u8() << 24
            | (long) u8() << 16
            | (long) u8() << 8
            | (long) u8() << 0;
    }

    public final long u32le() throws IOException {
        return (long) u8() << 0
            | (long) u8() << 8
            | (long) u8() << 16
            | (long) u8() << 24;
    }

    public long u48() throws IOException {
        return (long) u8() << 40
            | (long) u8() << 32
            | (long) u8() << 24
            | (long) u8() << 16
            | (long) u8() << 8
            | (long) u8() << 0;
    }

    public final double dbl() throws IOException {
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

    public final ByteString utf8() throws IOException {
        int len = u16();
        byte[] bytes = bytes(len);
        return new ByteString(bytes);
    }

    public final ByteString utf8long() throws IOException {
        int len = (int) u32();
        byte[] bytes = bytes(len);
        return new ByteString(bytes);
    }

    public static ASReader from(InputStream in) {
        return new StreamASReader(in);
    }

    public static ASReader from(byte[] data) {
        return new ByteArrayASReader(data, 0, data.length, 0);
    }

    public static ASReader from(byte[] data, int off, int len) {
        return new ByteArrayASReader(data, off, len, 0);
    }

}

@Accessors(fluent = true)
@RequiredArgsConstructor
class StreamASReader extends ASReader {
    private final InputStream in;
    private @Getter int bytesRead = 0;

    @Override
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

    @Override
    public int u8() throws IOException {
        int read = in.read();
        this.bytesRead++;
        if (read == -1) throw new EndOfStreamException("End of stream");
        return read;
    }

    @Override
    public ASReader limited(int len) {
        this.bytesRead += len;
        return new StreamASReader(new LimitedInputStream(this.in, len));
    }

}

@Accessors(fluent = true)
@AllArgsConstructor
class ByteArrayASReader extends ASReader {
    private final byte[] data;
    private int idx;
    private final int length;

    private @Getter int bytesRead;

    @Override
    public byte[] bytes(int len) throws IOException {
        if (this.bytesRead + len > this.length) {
            throw new EndOfStreamException("End of stream");
        }

        byte[] buf = new byte[len];
        System.arraycopy(this.data, this.idx, buf, 0, len);
        this.idx += len;
        this.bytesRead += len;
        return buf;
    }

    @Override
    public int u8() throws IOException {
        if (this.bytesRead >= this.length) {
            throw new EndOfStreamException("End of stream");
        }

        byte b = this.data[this.idx++];
        this.bytesRead++;
        return b & 0xFF;
    }

    @Override
    public ASReader limited(int len) {
        if (this.bytesRead + len > this.length) {
            throw new EndOfStreamException("End of stream");
        }

        this.idx += len;
        this.bytesRead += len;
        return new ByteArrayASReader(this.data, this.idx - len, len, 0);
    }

}
