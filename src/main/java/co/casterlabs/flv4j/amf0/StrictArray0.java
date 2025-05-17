package co.casterlabs.flv4j.amf0;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import co.casterlabs.commons.io.marshalling.PrimitiveMarshall;
import co.casterlabs.flv4j.util.ASSizer;
import co.casterlabs.flv4j.util.ASWriter;

// https://rtmp.veriskope.com/pdf/amf0-file-format-specification.pdf#page=7
public record StrictArray0(
    AMF0Type[] array
) implements AMF0Type {

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
            .marker()
            .u32();
        for (AMF0Type value : this.array) {
            sizer.size += value.size();
        }
        return sizer.size;
    }

    @Override
    public void serialize(OutputStream out) throws IOException {
        ASWriter.marker(out, this.type().id);
        ASWriter.u32(out, this.array.length);
        for (AMF0Type value : this.array) {
            value.serialize(out);
        }
    }

    @Override
    public final String toString() {
        return Arrays.toString(this.array);
    }

    static StrictArray0 from(int offset, byte[] bytes) {
        // We don't care about byte[offset + 0], which is the type.

        int arrayLen = PrimitiveMarshall.BIG_ENDIAN.bytesToInt(new byte[] {
                bytes[offset + 1],
                bytes[offset + 2],
                bytes[offset + 3],
                bytes[offset + 4]
        });

        offset += 1 + Integer.BYTES; // We're going to reuse this variable below.

        AMF0Type[] array = new AMF0Type[arrayLen];
        for (int arrayIdx = 0; arrayIdx < arrayLen; arrayIdx++) {
            AMF0Type.Type type = AMF0Type.Type.LUT[bytes[offset]];

            AMF0Type value = type.parse(offset, bytes);
            array[arrayIdx] = value;
            offset += value.size();
        }

        return new StrictArray0(array);
    }

}
