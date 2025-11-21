package co.casterlabs.flv4j.actionscript.io;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import lombok.NonNull;

// This class caches the UTF-8 byte representation of a string to optimize repeated access.
public record ByteString(@NonNull String string, @NonNull byte[] bytes) {

    public ByteString(@NonNull String string) {
        this(
            string,
            string.getBytes(StandardCharsets.UTF_8)
        );
    }

    public ByteString(@NonNull byte[] bytes) {
        this(
            new String(bytes, StandardCharsets.UTF_8),
            bytes
        );
    }

    public int stringLength() {
        return this.string.length();
    }

    public int byteLength() {
        return this.bytes.length;
    }

    @Override
    public String toString() {
        return this.string;
    }

    @Override
    public int hashCode() {
        return this.string.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (this == other) return true;

        if (other instanceof String) {
            return this.string.equals(other);
        }

        if (other instanceof ByteString b) {
            return this.string.equals(b.string);
        }

        if (other instanceof byte[] b) {
            return Arrays.equals(this.bytes, b);
        }

        return false;
    }

}
