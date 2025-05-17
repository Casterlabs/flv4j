package co.casterlabs.flv4j.amf0;

import java.io.IOException;
import java.io.OutputStream;

import co.casterlabs.flv4j.util.ASSizer;
import co.casterlabs.flv4j.util.ASWriter;

// https://rtmp.veriskope.com/pdf/amf0-file-format-specification.pdf#page=5
public record Boolean0(
    boolean value
) implements AMF0Type {
    public static final Boolean0 TRUE = new Boolean0(true);
    public static final Boolean0 FALSE = new Boolean0(false);

    private static final int SIZE = new ASSizer().marker().u8().size;

    @Override
    public Type type() {
        return Type.BOOLEAN;
    }

    @Override
    public int size() {
        return SIZE;
    }

    @Override
    public void serialize(OutputStream out) throws IOException {
        ASWriter.marker(out, this.type().id);
        ASWriter.u8(out, this.value ? 1 : 0);
    }

    @Override
    public final String toString() {
        return String.valueOf(this.value);
    }

    static Boolean0 from(int offset, byte[] bytes) {
        // We don't care about byte[offset + 0], which is the type.

        int value = bytes[offset + 1];
        return value == 0 ? FALSE : TRUE;
    }

}
