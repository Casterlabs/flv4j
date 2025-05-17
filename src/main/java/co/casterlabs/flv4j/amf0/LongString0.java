package co.casterlabs.flv4j.amf0;

import java.io.IOException;
import java.io.OutputStream;

import co.casterlabs.flv4j.util.ASReader;
import co.casterlabs.flv4j.util.ASSizer;
import co.casterlabs.flv4j.util.ASWriter;

// https://rtmp.veriskope.com/pdf/amf0-file-format-specification.pdf#page=7
public record LongString0(
    String value
) implements AMF0Type {

    @Override
    public Type type() {
        return Type.LONG_STRING;
    }

    @Override
    public int size() {
        return new ASSizer()
            .u8()
            .utf8long(this.value).size;
    }

    @Override
    public void serialize(OutputStream out) throws IOException {
        ASWriter.u8(out, this.type().id);
        ASWriter.utf8long(out, this.value);
    }

    @Override
    public final String toString() {
        return '"' + this.value + '"';
    }

    static LongString0 parse(ASReader reader) throws IOException {
        // marker is already consumed.

        String str = reader.utf8long();
        return new LongString0(str);
    }

}
