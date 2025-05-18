package co.casterlabs.flv4j.test.actionscript.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

public class UTF8 {

    @Test
    public void utf8() throws IOException {
        final String EXPECTED = stroflen(123);
        _Helper.rw((writer, reader) -> {
            writer.utf8(EXPECTED);
            String read = reader.utf8();
            assertEquals(EXPECTED, read);
        });
    }

    @Test
    public void utf8long() throws IOException {
        final String EXPECTED = stroflen(0xFFFF + 1); // >u16 limit.
        _Helper.rw((writer, reader) -> {
            writer.utf8long(EXPECTED);
            String read = reader.utf8long();
            assertEquals(EXPECTED, read);
        });
    }

    @Test
    public void utf8empty() throws IOException {
        _Helper.rw((writer, reader) -> {
            writer.utf8empty();
            String read = reader.utf8();
            assertEquals("", read);
        });
    }

    private static String stroflen(int len) {
        char[] str = new char[len];
        Arrays.fill(str, 'a');
        return new String(str);
    }

}
