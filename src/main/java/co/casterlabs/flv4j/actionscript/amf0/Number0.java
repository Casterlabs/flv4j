package co.casterlabs.flv4j.actionscript.amf0;

import java.io.IOException;

import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASSizer;
import co.casterlabs.flv4j.actionscript.io.ASWriter;

// https://rtmp.veriskope.com/pdf/amf0-file-format-specification.pdf#page=5
public record Number0(
    double value
) implements AMF0Type {
    private static final int SIZE = new ASSizer().u8().dbl().size;

    @Override
    public Type type() {
        return Type.NUMBER;
    }

    @Override
    public int size() {
        return SIZE;
    }

    @Override
    public void serialize(ASWriter writer) throws IOException {
        writer.u8(this.type().id);
        writer.dbl(this.value);
    }

    @Override
    public final String toString() {
        return String.valueOf(this.value);
    }

    static Number0 parse(ASReader reader) throws IOException {
        // marker is already consumed.

        double value = reader.dbl();
        return new Number0(value);
    }

}
