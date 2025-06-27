package co.casterlabs.flv4j.actionscript.amf0;

import java.io.IOException;

import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASSizer;
import co.casterlabs.flv4j.actionscript.io.ASWriter;

// https://rtmp.veriskope.com/pdf/amf0-file-format-specification.pdf#page=5
public record Boolean0(
    boolean value
) implements AMF0Type {
    public static final Boolean0 TRUE = new Boolean0(true);
    public static final Boolean0 FALSE = new Boolean0(false);

    private static final int SIZE = new ASSizer().u8().u8().size;

    @Override
    public Type type() {
        return Type.BOOLEAN;
    }

    @Override
    public int size() {
        return SIZE;
    }

    @Override
    public void serialize(ASWriter writer) throws IOException {
        writer.u8(this.type().id);
        writer.u8(this.value ? 1 : 0);
    }

    @Override
    public final String toString() {
        return String.valueOf(this.value);
    }

    static Boolean0 parse(ASReader reader) throws IOException {
        // marker is already consumed.

        int value = reader.u8();
        return value == 0 ? FALSE : TRUE;
    }

    public static Boolean0 valueOf(boolean value) {
        return value ? TRUE : FALSE;
    }

}
