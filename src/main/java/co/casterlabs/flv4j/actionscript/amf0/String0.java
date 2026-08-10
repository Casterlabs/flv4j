package co.casterlabs.flv4j.actionscript.amf0;

import java.io.IOException;

import co.casterlabs.flv4j.actionscript.amf0.AMF0Type.StringLike;
import co.casterlabs.flv4j.actionscript.io.ASAssert;
import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASSizer;
import co.casterlabs.flv4j.actionscript.io.ASWriter;
import co.casterlabs.flv4j.actionscript.io.ByteString;

// https://veovera.org/docs/legacy/amf0-file-format-spec.pdf#page=5
public record String0(
    ByteString value
) implements StringLike {
    public static final String0 EMPTY = new String0("");

    public String0(ByteString value) {
        ASAssert.u16(value.byteLength(), "string length");
        this.value = value;
    }

    public String0(String value) {
        this(new ByteString(value));
    }

    @Override
    public Type type() {
        return Type.STRING;
    }

    @Override
    public int size() {
        return ASSizer.u8 // type marker
            + ASSizer.utf8(this.value);
    }

    @Override
    public void serialize(ASWriter writer) throws IOException {
        writer.u8(this.type().id);
        writer.utf8(this.value);
    }

    @Override
    public final String toString() {
        return '"' + this.value.string() + '"';
    }

    static String0 parse(ASReader reader) throws IOException {
        // marker is already consumed.

        ByteString str = reader.utf8();
        return new String0(str);
    }

}
