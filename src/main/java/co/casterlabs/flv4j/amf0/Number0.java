package co.casterlabs.flv4j.amf0;

import java.io.IOException;
import java.io.OutputStream;

import co.casterlabs.flv4j.util.ASReader;
import co.casterlabs.flv4j.util.ASSizer;
import co.casterlabs.flv4j.util.ASWriter;

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
    public void serialize(OutputStream out) throws IOException {
        ASWriter.u8(out, this.type().id);
        ASWriter.dbl(out, this.value);
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
