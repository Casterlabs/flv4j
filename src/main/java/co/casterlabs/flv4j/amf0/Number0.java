package co.casterlabs.flv4j.amf0;

import java.io.IOException;
import java.io.OutputStream;

import co.casterlabs.commons.io.marshalling.PrimitiveMarshall;
import co.casterlabs.flv4j.util.ASSizer;
import co.casterlabs.flv4j.util.ASWriter;

// https://rtmp.veriskope.com/pdf/amf0-file-format-specification.pdf#page=5
public record Number0(
    double value
) implements AMF0Type {
    private static final int SIZE = new ASSizer().marker().dbl().size;

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
        ASWriter.marker(out, this.type().id);
        ASWriter.dbl(out, this.value);
    }

    @Override
    public final String toString() {
        return String.valueOf(this.value);
    }

    static Number0 from(int offset, byte[] bytes) {
        // We don't care about byte[offset + 0], which is the type.
        double value = PrimitiveMarshall.BIG_ENDIAN.bytesToDouble(new byte[] {
                bytes[offset + 1],
                bytes[offset + 2],
                bytes[offset + 3],
                bytes[offset + 4],
                bytes[offset + 5],
                bytes[offset + 6],
                bytes[offset + 7],
                bytes[offset + 8]
        });

        return new Number0(value);
    }

}
