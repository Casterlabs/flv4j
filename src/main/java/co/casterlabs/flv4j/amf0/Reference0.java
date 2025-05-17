package co.casterlabs.flv4j.amf0;

import java.io.IOException;
import java.io.OutputStream;

import co.casterlabs.commons.io.marshalling.PrimitiveMarshall;
import co.casterlabs.flv4j.util.ASSizer;
import co.casterlabs.flv4j.util.ASWriter;

// https://rtmp.veriskope.com/pdf/amf0-file-format-specification.pdf#page=6
/**
 * {@link #marker} is a pointer to a complex object. A complex object is defined
 * as an Object, TypedObject, StrictArray, or ECMAArray. You can check if a type
 * is complex via {@link AMF0Type#isComplex()}. To keep track of references,
 * create a table where the first complex object is stored at index 0. Each
 * subsequent complex object is stored at the next index.
 * 
 * @see <a href=
 *      "https://rtmp.veriskope.com/pdf/amf0-file-format-specification.pdf#page=6">AMF0
 *      File Format Specification</a>
 */
public record Reference0(
    int marker
) implements AMF0Type {
    private static final int SIZE = new ASSizer().marker().u16().size;

    @Override
    public Type type() {
        return Type.REFERENCE;
    }

    @Override
    public int size() {
        return SIZE;
    }

    @Override
    public void serialize(OutputStream out) throws IOException {
        ASWriter.marker(out, this.type().id);
        ASWriter.u16(out, this.marker);
    }

    @Override
    public final String toString() {
        return String.format("@%d", this.marker);
    }

    static Reference0 from(int offset, byte[] bytes) {
        // We don't care about byte[offset + 0], which is the type.
        int marker = PrimitiveMarshall.BIG_ENDIAN.bytesToInt(new byte[] {
                0,
                0,
                bytes[offset + 1],
                bytes[offset + 2]
        });

        return new Reference0(marker);
    }

}
