package co.casterlabs.flv4j.amf0;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import co.casterlabs.flv4j.amf0.AMF0Type.Type;
import co.casterlabs.flv4j.util.ASReader;
import co.casterlabs.flv4j.util.ASSizer;
import co.casterlabs.flv4j.util.ASWriter;

// https://rtmp.veriskope.com/pdf/amf0-file-format-specification.pdf#page=5
// https://rtmp.veriskope.com/pdf/amf0-file-format-specification.pdf#page=6
// This class is used for both Objects and ECMA Arrays.
class _ObjectUtils {

    static Map<String, AMF0Type> parseMap(ASReader reader) throws IOException {
        Map<String, AMF0Type> map = new LinkedHashMap<>();

        String key = null;
        while (true) {
            if (key == null) {
                String0 keyData = String0.parse(reader); // Type is implicit, which means no marker.
                key = keyData.value();
//                System.out.printf("[ECMA?]Object KEY: %d/%d @ %s\n", idx, bytes.length, key);
                continue;
            }

            AMF0Type value = AMF0Type.parse(reader);
            if (value.type() == Type.OBJECT_END) {
                if (key.length() > 0) {
                    throw new IllegalArgumentException("OBJECT_END must be preceeded by an empty key!");
                }

                // We're done!
                break;
            }

//            System.out.printf("[ECMA?]Object VALUE: %d/%d @ %d %s | %s\n", idx, bytes.length, value.size(), value.type(), value);

            map.put(key, value);
            key = null;
        }

        return Collections.unmodifiableMap(map);
    }

    static ASSizer computeMapSize(Map<String, AMF0Type> map) {
        ASSizer sizer = new ASSizer();

        for (Entry<String, AMF0Type> entry : map.entrySet()) {
            sizer.utf8(entry.getKey());
            sizer.size += entry.getValue().size();
        }

        sizer
            .utf8empty() // 0 key length (for the end tag)
            .u8();

        return sizer;
    }

    static void serializeMap(OutputStream out, Map<String, AMF0Type> map) throws IOException {
        for (Entry<String, AMF0Type> entry : map.entrySet()) {
            String key = entry.getKey();
            AMF0Type value = entry.getValue();

            ASWriter.utf8(out, key);
            value.serialize(out);
        }

        ASWriter.utf8empty(out); // 0 key length (for the end tag)
        ASWriter.u8(out, AMF0Type.Type.OBJECT_END.id);
    }

}
