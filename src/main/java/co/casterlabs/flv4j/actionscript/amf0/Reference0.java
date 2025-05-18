package co.casterlabs.flv4j.actionscript.amf0;

import java.io.IOException;

import co.casterlabs.flv4j.actionscript.io.ASAssert;
import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASSizer;
import co.casterlabs.flv4j.actionscript.io.ASWriter;

// https://rtmp.veriskope.com/pdf/amf0-file-format-specification.pdf#page=6
/**
 * {@link #index()} is a pointer to a complex object. A complex object is
 * defined as an Object, TypedObject, StrictArray, or ECMAArray. You can check
 * if a type is complex via {@link AMF0Type#isComplex()}. To keep track of
 * references, create a table where the first complex object is stored at index
 * 0. Each subsequent complex object is stored at the next index.
 * 
 * @see <a href=
 *      "https://rtmp.veriskope.com/pdf/amf0-file-format-specification.pdf#page=6">AMF0
 *      File Format Specification</a>
 */
public record Reference0(
    int index
) implements AMF0Type {
    private static final int SIZE = new ASSizer().u8().u16().size;

    public Reference0(int index) {
        ASAssert.u16(index, "index");
        this.index = index;
    }

    @Override
    public Type type() {
        return Type.REFERENCE;
    }

    @Override
    public int size() {
        return SIZE;
    }

    @Override
    public void serialize(ASWriter writer) throws IOException {
        writer.u8(this.type().id);
        writer.u16(this.index);
    }

    @Override
    public final String toString() {
        return String.format("@%d", this.index);
    }

    static Reference0 parse(ASReader reader) throws IOException {
        // marker is already consumed.

        int marker = reader.u16();
        return new Reference0(marker);
    }

}
