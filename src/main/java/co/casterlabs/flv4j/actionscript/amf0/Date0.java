package co.casterlabs.flv4j.actionscript.amf0;

import java.io.IOException;

import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASSizer;
import co.casterlabs.flv4j.actionscript.io.ASWriter;

// https://rtmp.veriskope.com/pdf/amf0-file-format-specification.pdf#page=5
public record Date0(
    double value
) implements AMF0Type {
    private static final int SIZE = new ASSizer().u8().u16().dbl().size;

    @Override
    public Type type() {
        return Type.DATE;
    }

    @Override
    public int size() {
        return SIZE;
    }

    @Override
    public void serialize(ASWriter writer) throws IOException {
        writer.u8(this.type().id);
        writer.u16(0); // Timezone is always 0.
        writer.dbl(this.value);
    }

    @Override
    public final String toString() {
        return String.valueOf(this.value);
    }

    static Date0 parse(ASReader reader) throws IOException {
        // marker is already consumed.
        reader.u16(); // We don't care about the timezone, it's always 0.

        double value = reader.dbl();
        return new Date0(value);
    }

}
