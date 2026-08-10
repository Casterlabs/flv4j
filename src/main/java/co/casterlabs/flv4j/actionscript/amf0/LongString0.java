package co.casterlabs.flv4j.actionscript.amf0;

import java.io.IOException;

import co.casterlabs.flv4j.actionscript.amf0.AMF0Type.StringLike;
import co.casterlabs.flv4j.actionscript.io.ASAssert;
import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASSizer;
import co.casterlabs.flv4j.actionscript.io.ASWriter;
import co.casterlabs.flv4j.actionscript.io.ByteString;

// https://veovera.org/docs/legacy/amf0-file-format-spec.pdf#page=7
public record LongString0(
    ByteString value
) implements StringLike {
    public static final LongString0 EMPTY = new LongString0("");

    public LongString0(ByteString value) {
        ASAssert.u32(value.byteLength(), "string length");
        this.value = value;
    }

    public LongString0(String value) {
        this(new ByteString(value));
    }

    @Override
    public Type type() {
        return Type.LONG_STRING;
    }

    @Override
    public int size() {
        return ASSizer.u8 // type marker
            + ASSizer.utf8long(this.value);
    }

    @Override
    public void serialize(ASWriter writer) throws IOException {
        writer.u8(this.type().id);
        writer.utf8long(this.value);
    }

    @Override
    public final String toString() {
        return '"' + this.value.string() + '"';
    }

    static LongString0 parse(ASReader reader) throws IOException {
        // marker is already consumed.

        ByteString str = reader.utf8long();
        return new LongString0(str);
    }

}
