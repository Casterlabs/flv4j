package co.casterlabs.flv4j.actionscript.amf0;

import java.io.IOException;

import co.casterlabs.flv4j.actionscript.amf0.AMF0Type.StringLike;
import co.casterlabs.flv4j.actionscript.io.ASAssert;
import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASSizer;
import co.casterlabs.flv4j.actionscript.io.ASWriter;

// https://rtmp.veriskope.com/pdf/amf0-file-format-specification.pdf#page=6
// This class is essentially a copy of LongString0.
public record XMLDocument0(
    String value
) implements StringLike {

    public XMLDocument0(String value) {
        ASAssert.u32(value.length(), "string length");
        this.value = value;
    }

    @Override
    public Type type() {
        return Type.XML_DOCUMENT;
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
    public final int hashCode() {
        return this.value.hashCode();
    }

    @Override
    public final boolean equals(Object other) {
        int hash = this.hashCode();
        int otherHash = other.hashCode();
        return hash == otherHash;
    }

    @Override
    public final String toString() {
        return '"' + this.value + '"';
    }

    public static XMLDocument0 parse(ASReader reader) throws IOException {
        // marker is already consumed.

        String str = reader.utf8long();
        return new XMLDocument0(str);
    }

}
