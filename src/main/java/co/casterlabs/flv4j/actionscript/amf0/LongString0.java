package co.casterlabs.flv4j.actionscript.amf0;

import java.io.IOException;

import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASSizer;
import co.casterlabs.flv4j.actionscript.io.ASWriter;

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
    public void serialize(ASWriter writer) throws IOException {
        writer.u8(this.type().id);
        writer.utf8long(this.value);
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
