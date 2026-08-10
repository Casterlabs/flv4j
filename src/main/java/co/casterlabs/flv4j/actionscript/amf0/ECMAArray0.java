package co.casterlabs.flv4j.actionscript.amf0;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import co.casterlabs.flv4j.actionscript.amf0.AMF0Type.ObjectLike;
import co.casterlabs.flv4j.actionscript.io.ASAssert;
import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASSizer;
import co.casterlabs.flv4j.actionscript.io.ASWriter;
import co.casterlabs.flv4j.actionscript.io.ByteString;

// https://veovera.org/docs/legacy/amf0-file-format-spec.pdf#page=6
public record ECMAArray0(
    Map<ByteString, AMF0Type> map
) implements ObjectLike {
    public static final ECMAArray0 EMPTY = new ECMAArray0(Map.of());

    public ECMAArray0(Map<ByteString, AMF0Type> map) {
        ASAssert.u32(map.size(), "map size");
        this.map = map;
    }

    public static ECMAArray0 of(Map<String, AMF0Type> map) {
        return new ECMAArray0(
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
        return Type.ECMA_ARRAY;
    }

    @Override
    public int size() {
        return ASSizer.u8 // type marker
            + ASSizer.u32 // size hint
            + _ObjectUtils.computeMapSize(this.map);
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

        Map<ByteString, AMF0Type> map = _ObjectUtils.parseMap(reader);
        return new ECMAArray0(map);
    }

}
