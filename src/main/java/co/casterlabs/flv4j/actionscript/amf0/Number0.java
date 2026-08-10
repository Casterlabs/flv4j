package co.casterlabs.flv4j.actionscript.amf0;

import java.io.IOException;

import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASSizer;
import co.casterlabs.flv4j.actionscript.io.ASWriter;

// https://veovera.org/docs/legacy/amf0-file-format-spec.pdf#page=5
public record Number0(
    double value
) implements AMF0Type {

    @Override
    public Type type() {
        return Type.NUMBER;
    }

    @Override
    public int size() {
        return ASSizer.u8  // type marker
            + ASSizer.dbl; // double value
    }

    @Override
    public void serialize(ASWriter writer) throws IOException {
        writer.u8(this.type().id);
        writer.dbl(this.value);
    }

    @Override
    public final String toString() {
        return String.valueOf(this.value);
    }

    static Number0 parse(ASReader reader) throws IOException {
        // marker is already consumed.

        double value = reader.dbl();
        return new Number0(value);
    }

}
