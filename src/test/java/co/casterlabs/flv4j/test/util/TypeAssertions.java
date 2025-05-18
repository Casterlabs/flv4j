package co.casterlabs.flv4j.test.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.IOException;

import co.casterlabs.flv4j.FLVSerializable;
import co.casterlabs.flv4j.actionscript.io.ASReader;

public class TypeAssertions {

    public static <T extends FLVSerializable> void assertReparse(T src, Parser<T> parser) throws IOException {
        byte[] srcBytes = src.raw();

        T reconstructed = parser.parse(new ASReader(srcBytes, 0, srcBytes.length));
        byte[] reconstructedBytes = reconstructed.raw();

        assertArrayEquals(srcBytes, reconstructedBytes, "Couldn't reparse: " + src.getClass().getSimpleName());
    }

    public static interface Parser<T> {

        public T parse(ASReader src) throws IOException;

    }

}
