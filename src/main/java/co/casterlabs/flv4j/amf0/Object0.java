package co.casterlabs.flv4j.amf0;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import co.casterlabs.flv4j.util.ASWriter;

// https://rtmp.veriskope.com/pdf/amf0-file-format-specification.pdf#page=5
public record Object0(
    Map<String, AMF0Type> map
) implements AMF0Type {

    @Override
    public boolean isComplex() {
        return true;
    }

    @Override
    public Type type() {
        return Type.OBJECT;
    }

    @Override
    public int size() {
        return _ObjectUtils.computeMapSize(this.map)
            .marker().size;
    }

    @Override
    public void serialize(OutputStream out) throws IOException {
        ASWriter.marker(out, this.type().id);
        _ObjectUtils.serializeMap(out, this.map);
    }

    @Override
    public final String toString() {
        return this.map.toString();
    }

    static Object0 from(int offset, byte[] bytes) {
        // We don't care about byte[offset + 0], which is the type.
        Map<String, AMF0Type> map = _ObjectUtils.parseMap(offset + 1, bytes);
        return new Object0(map);
    }

}
