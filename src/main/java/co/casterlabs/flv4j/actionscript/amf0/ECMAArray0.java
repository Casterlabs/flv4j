package co.casterlabs.flv4j.actionscript.amf0;

import java.io.IOException;
import java.util.Map;

import co.casterlabs.flv4j.actionscript.io.ASAssert;
import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASWriter;

// https://rtmp.veriskope.com/pdf/amf0-file-format-specification.pdf#page=6
public record ECMAArray0(
    Map<String, AMF0Type> map
) implements AMF0Type {

    public ECMAArray0(Map<String, AMF0Type> map) {
        ASAssert.u32(map.size(), "map size");
        this.map = map;
    }

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
    public void serialize(ASWriter writer) throws IOException {
        writer.u8(this.type().id);
        writer.u32(this.map.size());
        _ObjectUtils.serializeMap(writer, this.map);
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
