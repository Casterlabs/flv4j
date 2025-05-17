package co.casterlabs.flv4j.amf0;

import java.io.IOException;
import java.io.OutputStream;

import co.casterlabs.flv4j.util.ASReader;
import co.casterlabs.flv4j.util.ASSizer;
import co.casterlabs.flv4j.util.ASWriter;

// https://rtmp.veriskope.com/pdf/amf0-file-format-specification.pdf#page=5
public record String0(
    String value
) implements AMF0Type {

    @Override
    public Type type() {
        return Type.STRING;
    }

    @Override
    public int size() {
        return new ASSizer()
            .u8()
            .utf8(this.value).size;
    }

    @Override
    public void serialize(OutputStream out) throws IOException {
        ASWriter.u8(out, this.type().id);
        ASWriter.utf8(out, this.value);
    }

    @Override
    public final String toString() {
        return '"' + this.value + '"';
    }

    static String0 parse(ASReader reader) throws IOException {
        // marker is already consumed.

        String str = reader.utf8();
        return new String0(str);
    }

}
