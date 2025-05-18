package co.casterlabs.flv4j.actionscript.amf0;

import java.io.IOException;

import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASSizer;
import co.casterlabs.flv4j.actionscript.io.ASWriter;
import lombok.AllArgsConstructor;

// Marker types do not contain any data.
abstract class _MarkerType implements AMF0Type {
    private static final int SIZE = new ASSizer().u8().size;

    static _Parser parser(AMF0Type type) {
        return new StaticParser(type);
    }

    @Override
    public int size() {
        return SIZE;
    }

    @Override
    public byte[] raw() throws IOException {
        // Optimization
        return new byte[] {
                (byte) this.type().id
        };
    }

    @Override
    public void serialize(ASWriter writer) throws IOException {
        writer.u8(this.type().id);
    }

    @Override
    public final String toString() {
        return String.format("<%s>", this.type().name().toLowerCase());
    }

}

@AllArgsConstructor
class StaticParser implements _Parser {
    private final AMF0Type type;

    @Override
    public AMF0Type parse(ASReader reader) throws IOException {
        // marker is already consumed.
        return this.type;
    }

}
