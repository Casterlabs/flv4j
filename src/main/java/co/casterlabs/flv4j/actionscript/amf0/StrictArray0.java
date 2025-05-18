package co.casterlabs.flv4j.actionscript.amf0;

import java.io.IOException;
import java.util.Arrays;

import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASSizer;
import co.casterlabs.flv4j.actionscript.io.ASWriter;

// https://rtmp.veriskope.com/pdf/amf0-file-format-specification.pdf#page=7
public record StrictArray0(
    AMF0Type[] array
) implements AMF0Type {

    public StrictArray0(AMF0Type[] array) {
        // Bounds are already checked by Java's (signed) int limit.
        // Unfortunately we can't create arrays larger than that :/
        assert array != null : "array cannot be null";
        this.array = array;
    }

    @Override
    public boolean isComplex() {
        return true;
    }

    @Override
    public Type type() {
        return Type.STRICT_ARRAY;
    }

    @Override
    public int size() {
        ASSizer sizer = new ASSizer()
            .u8()
            .u32();
        for (AMF0Type value : this.array) {
            sizer.size += value.size();
        }
        return sizer.size;
    }

    @Override
    public void serialize(ASWriter writer) throws IOException {
        writer.u8(this.type().id);
        writer.u32(this.array.length);
        for (AMF0Type entry : this.array) {
            entry.serialize(writer);
        }
    }

    @Override
    public final String toString() {
        return Arrays.toString(this.array);
    }

    static StrictArray0 parse(ASReader reader) throws IOException {
        // marker is already consumed.

        int arrayLen = (int) reader.u32(); // We have to downcast because Java doesn't support > 2^32-1 arrays.

        AMF0Type[] array = new AMF0Type[arrayLen];
        for (int arrayIdx = 0; arrayIdx < arrayLen; arrayIdx++) {
            array[arrayIdx] = AMF0Type.parse(reader);
        }
        return new StrictArray0(array);
    }

}
