package co.casterlabs.flv4j.amf0;

import java.io.IOException;
import java.io.OutputStream;

import co.casterlabs.flv4j.util.ASSizer;
import co.casterlabs.flv4j.util.ASWriter;

// Marker types do not contain any data.
public abstract class _MarkerType implements AMF0Type {
    private static final int SIZE = new ASSizer().marker().size;

    private final byte[] raw = {
            (byte) this.type().id
    };

    @Override
    public int size() {
        return SIZE;
    }

    @Override
    public byte[] raw() throws IOException {
        return this.raw; // Optimization
    }

    @Override
    public void serialize(OutputStream out) throws IOException {
        ASWriter.marker(out, this.type().id);
    }

    @Override
    public final String toString() {
        return String.format("<%s>", this.type().name().toLowerCase());
    }

}
