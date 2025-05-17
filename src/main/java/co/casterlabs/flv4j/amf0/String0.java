package co.casterlabs.flv4j.amf0;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import co.casterlabs.commons.io.marshalling.PrimitiveMarshall;
import co.casterlabs.flv4j.util.ASSizer;
import co.casterlabs.flv4j.util.ASWriter;

// https://rtmp.veriskope.com/pdf/amf0-file-format-specification.pdf#page=5
public record String0(
    String value
) implements AMF0Type {

    @Override
    public Type type() {
        return Type.STRING;
    }

    @Override
    public int size() {
        return new ASSizer()
            .marker()
            .utf8(this.value).size;
    }

    @Override
    public void serialize(OutputStream out) throws IOException {
        ASWriter.marker(out, this.type().id);
        ASWriter.utf8(out, this.value);
    }

    @Override
    public final String toString() {
        return '"' + this.value + '"';
    }

    static String0 from(int offset, byte[] bytes) {
        // We don't care about byte[offset + 0], which is the type.
        int len = PrimitiveMarshall.BIG_ENDIAN.bytesToInt(new byte[] {
                0,
                0,
                bytes[offset + 1],
                bytes[offset + 2]
        });

        String str = new String(bytes, offset + 1 + Short.BYTES, len, StandardCharsets.UTF_8);
        return new String0(str);
    }

}
