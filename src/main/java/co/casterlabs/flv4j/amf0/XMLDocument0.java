package co.casterlabs.flv4j.amf0;

import java.io.IOException;
import java.io.OutputStream;

import co.casterlabs.flv4j.util.ASReader;
import co.casterlabs.flv4j.util.ASSizer;
import co.casterlabs.flv4j.util.ASWriter;

// https://rtmp.veriskope.com/pdf/amf0-file-format-specification.pdf#page=6
// This class is essentially a copy of LongString0.
public record XMLDocument0(
    String value
) implements AMF0Type {

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
    public void serialize(OutputStream out) throws IOException {
        ASWriter.u8(out, this.type().id);
        ASWriter.utf8long(out, this.value);
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
