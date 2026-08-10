package co.casterlabs.flv4j.actionscript.amf0;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import co.casterlabs.flv4j.actionscript.amf0.AMF0Type.ObjectLike;
import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASSizer;
import co.casterlabs.flv4j.actionscript.io.ASWriter;
import co.casterlabs.flv4j.actionscript.io.ByteString;

// https://veovera.org/docs/legacy/amf0-file-format-spec.pdf#page=8
public record TypedObject0(
    ByteString className,
    Map<ByteString, AMF0Type> map
) implements ObjectLike {

    public TypedObject0(ByteString className) {
        this(className, Collections.emptyMap());
    }

    public TypedObject0(ByteString className, Map<ByteString, AMF0Type> map) {
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
        return ASSizer.u8 // type marker
            + ASSizer.utf8(this.className)
            + _ObjectUtils.computeMapSize(this.map);
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

        ByteString className = reader.utf8();
        Map<ByteString, AMF0Type> map = _ObjectUtils.parseMap(reader);

        return new TypedObject0(className, map);
    }

}
