package co.casterlabs.flv4j.amf0;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import co.casterlabs.flv4j.util.ASReader;
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
            .u8()
            .u32().size;
    }

    @Override
    public void serialize(OutputStream out) throws IOException {
        ASWriter.u8(out, this.type().id);
        ASWriter.u32(out, this.map.size());
        _ObjectUtils.serializeMap(out, this.map);
    }

    @Override
    public final String toString() {
        return this.map.toString();
    }

    static ECMAArray0 parse(ASReader reader) throws IOException {
        // marker is already consumed.
        reader.u32(); // We don't care about the size hint, it's a suggestion.

        Map<String, AMF0Type> map = _ObjectUtils.parseMap(reader);
        return new ECMAArray0(map);
    }

}
