package co.casterlabs.flv4j.actionscript.amf0;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import co.casterlabs.flv4j.actionscript.amf0.AMF0Type.ObjectLike;
import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASSizer;
import co.casterlabs.flv4j.actionscript.io.ASWriter;
import co.casterlabs.flv4j.actionscript.io.ByteString;

// https://veovera.org/docs/legacy/amf0-file-format-spec.pdf#page=5
public record Object0(
    Map<ByteString, AMF0Type> map
) implements ObjectLike {
    public static final Object0 EMPTY = new Object0(Map.of());

    public Object0(Map<ByteString, AMF0Type> map) {
        assert map != null : "map cannot be null";
        this.map = map;
    }

    public static Object0 of(Map<String, AMF0Type> map) {
        return new Object0(
            map.entrySet()
                .stream()
                .collect(
                    Collectors.toMap(
                        e -> new ByteString(e.getKey()),
                        Map.Entry::getValue
                    )
                )
        );
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
        return ASSizer.u8 // type marker
            + _ObjectUtils.computeMapSize(this.map);
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

        Map<ByteString, AMF0Type> map = _ObjectUtils.parseMap(reader);
        return new Object0(map);
    }

}
