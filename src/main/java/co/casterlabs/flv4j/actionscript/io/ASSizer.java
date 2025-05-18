package co.casterlabs.flv4j.actionscript.io;

import java.nio.charset.StandardCharsets;

public class ASSizer {
    public int size = 0;

    public ASSizer bytes(int count) {
        this.size += count;
        return this;
    }

    public ASSizer u8() {
        this.size += 1;
        return this;
    }

    public ASSizer u16() {
        this.size += 2;
        return this;
    }

    public ASSizer s16() {
        this.size += 2;
        return this;
    }

    public ASSizer u24() {
        this.size += 3;
        return this;
    }

    public ASSizer u29(int value) {
        // Single byte: (0-127) (inclusive)
        if (value < 128) {
            this.size += 1;
            return this;
        }

        // Two bytes: 128-16383 (inclusive)
        if (value < 16384) {
            this.size += 2;
            return this;
        }

        // Three bytes: 16384-2097151 (inclusive)
        if (value < 2097152) {
            this.size += 3;
            return this;
        }

        // Four bytes: 2097152-536870911 (inclusive)
        this.size += 4;
        return this;
    }

    public ASSizer u32() {
        this.size += 4;
        return this;
    }

    public ASSizer dbl() {
        this.size += 8;
        return this;
    }

    public ASSizer utf8(String str) {
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);

        this.u16();
        this.size += strBytes.length;
        return this;
    }

    public ASSizer utf8long(String str) {
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);
        this.u32();
        this.size += strBytes.length;
        return this;
    }

    public ASSizer utf8empty() {
        return this.u16();  // A u16 value of 0.
    }

}
