package co.casterlabs.flv4j.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import co.casterlabs.flv4j.test.util.ASIOAction;

public class ActionScriptIO {
    // These test values should be a pattern that is easy to detect incorrect shifts
    // or incorrect byte ordering.
    private static final double DBL = 3.512700564088504e-303d;
    private static final int BITS = 0b00000001_00100011_01000101_01100111;

    @Test
    void u8() throws IOException {
        ASIOAction.rw((writer, reader) -> {
            writer.u8(BITS & 0xFF);
            int read = reader.u8();
            assertEquals(BITS & 0xFF, read);
        });
    }

    @Test
    void u16() throws IOException {
        ASIOAction.rw((writer, reader) -> {
            writer.u16(BITS & 0xFFFF);
            int read = reader.u16();
            assertEquals(BITS & 0xFFFF, read);
        });
    }

    @Test
    void s16() throws IOException {
        final short EXPECTED = (short) BITS & 0xFFFF;
        ASIOAction.rw((writer, reader) -> {
            writer.s16(EXPECTED);
            short read = reader.s16();
            assertEquals(EXPECTED, read);
        });
    }

    @Test
    void u24() throws IOException {
        ASIOAction.rw((writer, reader) -> {
            writer.u24(BITS & 0xFFFFFF);
            int read = reader.u24();
            assertEquals(BITS & 0xFFFFFF, read);
        });
    }

    @Test
    void u32() throws IOException {
        ASIOAction.rw((writer, reader) -> {
            writer.u32(BITS);
            long read = reader.u32();
            assertEquals(BITS, read);
        });
    }

    @Test
    void dbl() throws IOException {
        ASIOAction.rw((writer, reader) -> {
            writer.dbl(DBL);
            double read = reader.dbl();
            assertEquals(DBL, read);
        });
    }

    @Test
    void utf8() throws IOException {
        ASIOAction.rw((writer, reader) -> {
            final String VALUE = str(12);
            writer.utf8(VALUE);
            String read = reader.utf8();
            assertEquals(VALUE, read);
        });
    }

    @Test
    void utf8long() throws IOException {
        ASIOAction.rw((writer, reader) -> {
            final String VALUE = str(0xFFFF + 1); // >u16 limit.
            writer.utf8long(VALUE);
            String read = reader.utf8long();
            assertEquals(VALUE, read);
        });
    }

    @Test
    void utf8empty() throws IOException {
        ASIOAction.rw((writer, reader) -> {
            writer.utf8empty();
            String read = reader.utf8();
            assertEquals("", read);
        });
    }

    private static String str(int len) {
        char[] str = new char[len];
        Arrays.fill(str, 'a');
        return new String(str);
    }

}
