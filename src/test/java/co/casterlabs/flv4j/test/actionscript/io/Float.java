package co.casterlabs.flv4j.test.actionscript.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class Float {
    // These test values should be a pattern that is easy to detect incorrect shifts
    // or incorrect byte ordering.
    private static final double DBL = 3.512700564088504e-303d;

    @Test
    public void dbl() throws IOException {
        _Helper.rw((writer, reader) -> {
            writer.dbl(DBL);
            double read = reader.dbl();
            assertEquals(DBL, read);
        });
    }

}
