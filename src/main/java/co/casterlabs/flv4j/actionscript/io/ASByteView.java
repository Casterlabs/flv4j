package co.casterlabs.flv4j.actionscript.io;

import java.util.Arrays;

public record ASByteView(byte[] buffer, int offset, int length) {

    public ASByteView(byte[] buf) {
        this(buf, 0, buf.length);
    }

    public ASByteView slice(int newOffset) {
        return this.slice(newOffset, this.length - newOffset);
    }

    public ASByteView slice(int newOffset, int newLength) {
        return new ASByteView(this.buffer, this.offset + newOffset, newLength);
    }

    public byte[] raw() {
        byte[] result = new byte[this.length];
        System.arraycopy(this.buffer, this.offset, result, 0, this.length);
        return result;
    }

    public byte[] bytes(int index, int len) {
        byte[] result = new byte[len];
        System.arraycopy(this.buffer, this.offset + index, result, 0, len);
        return result;
    }

    public int u8(int index) {
        return this.buffer[this.offset + index] & 0xFF;
    }

    public int u16(int index) {
        return u8(index + 0) << 8
            | u8(index + 1);
    }

    public short s16(int index) {
        return (short) u16(index);
    }

    public int u24(int index) {
        return u8(index + 0) << 16
            | u8(index + 1) << 8
            | u8(index + 2);
    }

    public int s24(int index) {
        return (u24(index) << 8) >> 8; // Sign extend
    }

    public int u29(int index) {
        int b1 = u8(index + 0);
        int result = b1 & 0x7F;
        if ((b1 & 0x80) == 0) {
            return result;
        }

        int b2 = u8(index + 1);
        result = (result << 7) | (b2 & 0x7F);
        if ((b2 & 0x80) == 0) {
            return result;
        }

        int b3 = u8(index + 2);
        result = (result << 7) | (b3 & 0x7F);
        if ((b3 & 0x80) == 0) {
            return result;
        }

        // NB: All 8 bits are used in the final byte, so we DO NOT shift by 7.
        int b4 = u8(index + 3);
        return (result << 8) | b4;
    }

    public int u29Length(int index) {
        int b1 = u8(index + 0);
        if ((b1 & 0x80) == 0) {
            return 1;
        }

        int b2 = u8(index + 1);
        if ((b2 & 0x80) == 0) {
            return 2;
        }

        int b3 = u8(index + 2);
        if ((b3 & 0x80) == 0) {
            return 3;
        }

        return 4;
    }

    public long u32(int index) {
        return (long) u8(index + 0) << 24
            | (long) u8(index + 1) << 16
            | (long) u8(index + 2) << 8
            | (long) u8(index + 3) << 0;
    }

    public long u32le(int index) {
        return (long) u8(index + 0) << 0
            | (long) u8(index + 1) << 8
            | (long) u8(index + 2) << 16
            | (long) u8(index + 3) << 24;
    }

    public long u48(int index) {
        return (long) u8(index + 0) << 40
            | (long) u8(index + 1) << 32
            | (long) u8(index + 2) << 24
            | (long) u8(index + 3) << 16
            | (long) u8(index + 4) << 8
            | (long) u8(index + 5) << 0;
    }

    public double dbl(int index) {
        long bits = (long) u8(index + 0) << 56
            | (long) u8(index + 1) << 48
            | (long) u8(index + 2) << 40
            | (long) u8(index + 3) << 32
            | (long) u8(index + 4) << 24
            | (long) u8(index + 5) << 16
            | (long) u8(index + 6) << 8
            | (long) u8(index + 7) << 0;
        return Double.longBitsToDouble(bits);
    }

    public ByteString utf8(int index) {
        int len = u16(index);
        byte[] bytes = bytes(index + 2, len);
        return new ByteString(bytes);
    }

    public ByteString utf8long(int index) {
        int len = (int) u32(index);
        byte[] bytes = bytes(index + 4, len);
        return new ByteString(bytes);
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj instanceof ASByteView other) {
            if (this == obj) return true;
            if (this.length != other.length) return false;

            return Arrays.equals(
                this.buffer, this.offset, this.offset + this.length,
                other.buffer, other.offset, other.offset + other.length
            );
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = 1;
        for (int i = offset; i < this.length; i++) {
            result = 31 * result + this.u8(i);
        }
        return result;
    }

}
