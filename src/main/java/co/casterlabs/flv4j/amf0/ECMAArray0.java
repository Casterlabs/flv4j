package co.casterlabs.flv4j.amf0;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import co.casterlabs.flv4j.util.ASWriter;

// https://rtmp.veriskope.com/pdf/amf0-file-format-specification.pdf#page=6
public record ECMAArray0(
    Map<String, AMF0Type> map
) implements AMF0Type {

    @Override
    public boolean isComplex() {
        return true;
    }

    @Override
    public Type type() {
        return Type.ECMA_ARRAY;
    }

    @Override
    public int size() {
        return _ObjectUtils.computeMapSize(this.map)
            .marker()
            .u32().size;
    }

    @Override
    public void serialize(OutputStream out) throws IOException {
        ASWriter.marker(out, this.type().id);
        ASWriter.u32(out, this.map.size());
        _ObjectUtils.serializeMap(out, this.map);
    }

    @Override
    public final String toString() {
        return this.map.toString();
    }

    static ECMAArray0 from(int offset, byte[] bytes) {
        // We don't care about byte[offset + 0], which is the type.
        // We also don't care about the size hint (bytes [offset + 1] -> [offset + 4]).
        Map<String, AMF0Type> map = _ObjectUtils.parseMap(offset + 1 + Integer.BYTES, bytes);
        return new ECMAArray0(map);
    }

}
