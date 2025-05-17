package co.casterlabs.flv4j.amf0;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import co.casterlabs.commons.io.marshalling.PrimitiveMarshall;
import co.casterlabs.flv4j.util.ASWriter;

// https://rtmp.veriskope.com/pdf/amf0-file-format-specification.pdf#page=8
public record TypedObject0(
    String className,
    Map<String, AMF0Type> map
) implements AMF0Type {

    @Override
    public boolean isComplex() {
        return true;
    }

    @Override
    public Type type() {
        return Type.TYPED_OBJECT;
    }

    @Override
    public int size() {
        return _ObjectUtils.computeMapSize(this.map)
            .marker()
            .utf8(this.className).size;
    }

    @Override
    public void serialize(OutputStream out) throws IOException {
        ASWriter.marker(out, this.type().id);
        ASWriter.utf8(out, this.className);
        _ObjectUtils.serializeMap(out, this.map);
    }

    @Override
    public final String toString() {
        return this.map.toString();
    }

    static TypedObject0 from(int offset, byte[] bytes) {
        // We don't care about byte[offset + 0], which is the type.

        int classNameLen = PrimitiveMarshall.BIG_ENDIAN.bytesToInt(new byte[] {
                0,
                0,
                bytes[offset + 1],
                bytes[offset + 2]
        });

        String className = new String(bytes, offset + 1 + Integer.BYTES, classNameLen, StandardCharsets.UTF_8);
        Map<String, AMF0Type> map = _ObjectUtils.parseMap(offset + 1 + Short.BYTES + classNameLen, bytes);

        return new TypedObject0(className, map);
    }

}
