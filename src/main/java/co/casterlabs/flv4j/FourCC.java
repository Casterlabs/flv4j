package co.casterlabs.flv4j;

import java.io.IOException;

import co.casterlabs.flv4j.actionscript.io.ASAssert;
import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASSizer;
import co.casterlabs.flv4j.actionscript.io.ASWriter;

public record FourCC(long bits, String string) implements FLVSerializable {

    public FourCC(long bits, String string) {
        ASAssert.u32(bits, "bits");
        assert string.length() == 4 : "Invalid FourCC length: " + string.length();
        this.bits = bits;
        this.string = string;
    }

    public FourCC(long bits) {
        this(bits, bitsToString(bits));
    }

    public FourCC(String string) {
        this(stringToBits(string), string);
    }

    @Override
    public int size() {
        return ASSizer.u32;
    }

    @Override
    public void serialize(ASWriter writer) throws IOException {
        writer.u32(this.bits);
    }

    public static FourCC parse(ASReader reader) throws IOException {
        return new FourCC(reader.u32());
    }

    @Override
    public String toString() {
        return this.string;
    }

    private static final long stringToBits(String string) {
        assert string.length() == 4 : "FourCC string must be exactly 4 characters, got: " + string.length();
        int a = string.charAt(0);
        int b = string.charAt(1);
        int c = string.charAt(2);
        int d = string.charAt(3);
        assert (a | b | c | d) <= 0x7F : "FourCC string must contain only 7-bit ASCII characters: " + string;
        return (long) a << 24 | (long) b << 16 | (long) c << 8 | d;
    }

    private static final String bitsToString(long bits) {
        int a = (int) (bits >> 24) & 0xFF;
        int b = (int) (bits >> 16) & 0xFF;
        int c = (int) (bits >> 8) & 0xFF;
        int d = (int) bits & 0xFF;
        return String.format("%c%c%c%c", (char) a, (char) b, (char) c, (char) d);
    }

}
