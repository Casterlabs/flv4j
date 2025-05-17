package co.casterlabs.flv4j.amf0;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import co.casterlabs.commons.io.marshalling.PrimitiveMarshall;
import co.casterlabs.flv4j.util.ASSizer;
import co.casterlabs.flv4j.util.ASWriter;

// https://rtmp.veriskope.com/pdf/amf0-file-format-specification.pdf#page=7
public record LongString0(
    String value
) implements AMF0Type {

    @Override
    public Type type() {
        return Type.LONG_STRING;
    }

    @Override
    public int size() {
        return new ASSizer()
            .marker()
            .utf8long(this.value).size;
    }

    @Override
    public void serialize(OutputStream out) throws IOException {
        ASWriter.marker(out, this.type().id);
        ASWriter.utf8long(out, this.value);
    }

    @Override
    public final String toString() {
        return '"' + this.value + '"';
    }

    static LongString0 from(int offset, byte[] bytes) {
        // We don't care about byte[offset + 0], which is the type.
        int len = PrimitiveMarshall.BIG_ENDIAN.bytesToInt(new byte[] {
                bytes[offset + 1],
                bytes[offset + 2],
                bytes[offset + 3],
                bytes[offset + 4]
        });

        String str = new String(bytes, offset + 1 + Integer.BYTES, len, StandardCharsets.UTF_8);
        return new LongString0(str);
    }

}
