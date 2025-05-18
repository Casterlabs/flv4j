package co.casterlabs.flv4j.test.actionscript.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class Integer {
    // These test values should be a pattern that is easy to detect incorrect shifts
    // or incorrect byte ordering.
    private static final int BITS = 0b00000001_00100011_01000101_01100111;

    @Test
    public void u8() throws IOException {
        int EXPECTED = BITS & 0xFF;
        _Helper.rw((writer, reader) -> {
            writer.u8(EXPECTED);
            int read = reader.u8();
            assertEquals(EXPECTED, read);
        });
    }

    @Test
    public void u16() throws IOException {
        final int EXPECTED = BITS & 0xFFFF;
        _Helper.rw((writer, reader) -> {
            writer.u16(EXPECTED);
            int read = reader.u16();
            assertEquals(EXPECTED, read);
        });
    }

    @Test
    public void s16() throws IOException {
        final short EXPECTED = (short) BITS & 0xFFFF;
        _Helper.rw((writer, reader) -> {
            writer.s16(EXPECTED);
            short read = reader.s16();
            assertEquals(EXPECTED, read);
        });
    }

    @Test
    public void u24() throws IOException {
        final int EXPECTED = BITS & 0xFFFFFF;
        _Helper.rw((writer, reader) -> {
            writer.u24(EXPECTED);
            int read = reader.u24();
            assertEquals(EXPECTED, read);
        });
    }

    @Test
    public void u32() throws IOException {
        final int EXPECTED = BITS & 0xFFFFFFFF;
        _Helper.rw((writer, reader) -> {
            writer.u32(EXPECTED);
            long read = reader.u32();
            assertEquals(EXPECTED, read);
        });
    }

}
