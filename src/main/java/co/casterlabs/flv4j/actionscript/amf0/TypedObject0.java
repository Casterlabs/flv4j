package co.casterlabs.flv4j.actionscript.amf0;

import java.io.IOException;
import java.util.Map;

import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASWriter;

// https://rtmp.veriskope.com/pdf/amf0-file-format-specification.pdf#page=8
public record TypedObject0(
    String className,
    Map<String, AMF0Type> map
) implements AMF0Type {

    public TypedObject0(String className, Map<String, AMF0Type> map) {
        assert className != null : "className cannot be null";
        assert map != null : "map cannot be null";
        this.className = className;
        this.map = map;
    }

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
    public void serialize(ASWriter writer) throws IOException {
        writer.u8(this.type().id);
        writer.utf8(this.className);
        _ObjectUtils.serializeMap(writer, this.map);
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
