package co.casterlabs.flv4j.test.actionscript.io;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.Test;

import co.casterlabs.flv4j.actionscript.io.ASSizer;

public class U29 {
    // @formatter:off
    private static final int BITS7  =                            0b01111111;
    private static final int BITS14 =                   0b00111111_11111111;
    private static final int BITS21 =          0b00011111_11111111_11111111;
    private static final int BITS29 = 0b00011111_11111111_11111111_11111111;
    // @formatter:on

    @Test
    public void u29_fuzz() throws IOException {
        final int U29_MAX = BITS29;
        final int MAX_ITER = 1000000;
        for (int i = 0; i < MAX_ITER; i++) {
            final int EXPECTED = ThreadLocalRandom.current().nextInt(U29_MAX);
            int byteLen = _Helper.rw((writer, reader) -> {
                writer.u29(EXPECTED);
                int read = reader.u29();
                assertEquals(EXPECTED, read);
            }).length;
            assertEquals(new ASSizer().u29(EXPECTED).size, byteLen);
        }
    }

    @Test
    public void u29_1() throws IOException {
        final int EXPECTED = BITS7;
        final int LEN = 1;
        int byteLen = _Helper.rw((writer, reader) -> {
            writer.u29(EXPECTED);
            int read = reader.u29();
            assertEquals(EXPECTED, read);
        }).length;
        assertEquals(byteLen, LEN);
        assertEquals(new ASSizer().u29(EXPECTED).size, LEN);
    }

    @Test
    public void u29_2() throws IOException {
        final int EXPECTED = BITS14;
        final int LEN = 2;
        int byteLen = _Helper.rw((writer, reader) -> {
            writer.u29(EXPECTED);
            int read = reader.u29();
            assertEquals(EXPECTED, read);
        }).length;
        assertEquals(byteLen, LEN);
        assertEquals(new ASSizer().u29(EXPECTED).size, LEN);
    }

    @Test
    public void u29_3() throws IOException {
        final int EXPECTED = BITS21;
        final int LEN = 3;
        int byteLen = _Helper.rw((writer, reader) -> {
            writer.u29(EXPECTED);
            int read = reader.u29();
            assertEquals(EXPECTED, read);
        }).length;
        assertEquals(byteLen, LEN);
        assertEquals(new ASSizer().u29(EXPECTED).size, LEN);
    }

    @Test
    public void u29_4() throws IOException {
        final int EXPECTED = BITS29;
        final int LEN = 4;
        int byteLen = _Helper.rw((writer, reader) -> {
            writer.u29(EXPECTED);
            int read = reader.u29();
            assertEquals(EXPECTED, read);
        }).length;
        assertEquals(byteLen, LEN);
        assertEquals(new ASSizer().u29(EXPECTED).size, LEN);
    }

    /* -------------------- */
    /*    Checking bytes    */
    /* -------------------- */

    @Test
    public void u29_1_bytes() throws IOException {
        final int VALUE = 0b01111111;
        final byte[] EXPECTED_BYTES = new byte[] {
                (byte) 0b01111111
        };
        byte[] bytes = _Helper.rw((writer, reader) -> {
            writer.u29(VALUE);
        });
        assertArrayEquals(EXPECTED_BYTES, bytes);
    }

    @Test
    public void u29_2_bytes() throws IOException {
        final int VALUE = 0b10000001;
        final byte[] EXPECTED_BYTES = new byte[] {
                (byte) 0b10000001,
                (byte) 0b00000001
        };
        byte[] bytes = _Helper.rw((writer, reader) -> {
            writer.u29(VALUE);
        });
        assertArrayEquals(EXPECTED_BYTES, bytes);
    }

    @Test
    public void u29_3_bytes() throws IOException {
        final int VALUE = 0b00000000_01000000_10000001;
        final byte[] EXPECTED_BYTES = new byte[] {
                (byte) 0b10000001,
                (byte) 0b10000001,
                (byte) 0b00000001
        };
        byte[] bytes = _Helper.rw((writer, reader) -> {
            writer.u29(VALUE);
        });
        assertArrayEquals(EXPECTED_BYTES, bytes);
    }

    @Test
    public void u29_4_bytes() throws IOException {
        final int VALUE = 0b00000000_01000000_10000001_00000001;
        final byte[] EXPECTED_BYTES = new byte[] {
                (byte) 0b10000001,
                (byte) 0b10000001,
                (byte) 0b10000001,
                (byte) 0b00000001
        };
        byte[] bytes = _Helper.rw((writer, reader) -> {
            writer.u29(VALUE);
        });
        assertArrayEquals(EXPECTED_BYTES, bytes);
    }

}
