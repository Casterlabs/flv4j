package co.casterlabs.flv4j.actionscript.amf0;

import java.io.IOException;

import co.casterlabs.flv4j.actionscript.amf0.AMF0Type.StringLike;
import co.casterlabs.flv4j.actionscript.io.ASAssert;
import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASSizer;
import co.casterlabs.flv4j.actionscript.io.ASWriter;

// https://rtmp.veriskope.com/pdf/amf0-file-format-specification.pdf#page=5
public record String0(
    String value
) implements StringLike {
    public static final String0 EMPTY = new String0("");

    public String0(String value) {
        ASAssert.u16(value.length(), "string length");
        this.value = value;
    }

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
    public void serialize(ASWriter writer) throws IOException {
        writer.u8(this.type().id);
        writer.utf8(this.value);
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
