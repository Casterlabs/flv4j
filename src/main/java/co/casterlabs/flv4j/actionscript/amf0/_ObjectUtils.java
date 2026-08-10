package co.casterlabs.flv4j.actionscript.amf0;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import co.casterlabs.flv4j.actionscript.amf0.AMF0Type.Type;
import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASSizer;
import co.casterlabs.flv4j.actionscript.io.ASWriter;
import co.casterlabs.flv4j.actionscript.io.ByteString;

// https://rtmp.veriskope.com/pdf/amf0-file-format-specification.pdf#page=5
// https://rtmp.veriskope.com/pdf/amf0-file-format-specification.pdf#page=6
// This class is used for both Objects and ECMA Arrays.
class _ObjectUtils {

    static Map<ByteString, AMF0Type> parseMap(ASReader reader) throws IOException {
        Map<ByteString, AMF0Type> map = new LinkedHashMap<>();
        while (true) {
            ByteString key = String0.parse(reader).value(); // NB: Type is implicit, which means no marker.
            AMF0Type value = AMF0Type.parse(reader);

            if (value.type() == Type.OBJECT_END) {
                if (key.byteLength() > 0) {
                    throw new IllegalArgumentException("OBJECT_END must be preceeded by an empty key!");
                }
                break; // We're done!
            }

            map.put(key, value);
        }
        return Collections.unmodifiableMap(map);
    }

    static int computeMapSize(Map<ByteString, AMF0Type> map) {
        int size = 0;

        for (Entry<ByteString, AMF0Type> entry : map.entrySet()) {
            size += ASSizer.utf8(entry.getKey());
            size += entry.getValue().size();
        }

        size += ASSizer.utf8empty; // 0 key length (for the end tag)
        size += ASSizer.u8; // OBJECT_END marker

        return size;
    }

    static void serializeMap(ASWriter writer, Map<ByteString, AMF0Type> map) throws IOException {
        for (Entry<ByteString, AMF0Type> entry : map.entrySet()) {
            ByteString key = entry.getKey();
            AMF0Type value = entry.getValue();

            writer.utf8(key);
            value.serialize(writer);
        }

        writer.utf8empty(); // 0 key length (for the end tag)
        writer.u8(AMF0Type.Type.OBJECT_END.id);
    }

}
