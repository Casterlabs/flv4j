package co.casterlabs.flv4j.actionscript.amf0;

import java.io.IOException;
import java.util.Map;

import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASWriter;

// https://rtmp.veriskope.com/pdf/amf0-file-format-specification.pdf#page=5
public record Object0(
    Map<String, AMF0Type> map
) implements AMF0Type {

    public Object0(Map<String, AMF0Type> map) {
        assert map != null : "map cannot be null";
        this.map = map;
    }

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
            .u8().size;
    }

    @Override
    public void serialize(ASWriter writer) throws IOException {
        writer.u8(this.type().id);
        _ObjectUtils.serializeMap(writer, this.map);
    }

    @Override
    public final String toString() {
        return this.map.toString();
    }

    static Object0 parse(ASReader reader) throws IOException {
        // marker is already consumed.

        Map<String, AMF0Type> map = _ObjectUtils.parseMap(reader);
        return new Object0(map);
    }

}
