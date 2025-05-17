package co.casterlabs.flv4j.amf0;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import co.casterlabs.flv4j.util.ASReader;
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
            .u8()
            .utf8(this.className).size;
    }

    @Override
    public void serialize(OutputStream out) throws IOException {
        ASWriter.u8(out, this.type().id);
        ASWriter.utf8(out, this.className);
        _ObjectUtils.serializeMap(out, this.map);
    }

    @Override
    public final String toString() {
        return this.map.toString();
    }

    static TypedObject0 parse(ASReader reader) throws IOException {
        // marker is already consumed.

        String className = reader.utf8();
        Map<String, AMF0Type> map = _ObjectUtils.parseMap(reader);

        return new TypedObject0(className, map);
    }

}
