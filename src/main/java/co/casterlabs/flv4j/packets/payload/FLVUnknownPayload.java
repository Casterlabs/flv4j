package co.casterlabs.flv4j.packets.payload;

import java.io.IOException;
import java.io.OutputStream;

public record FLVUnknownPayload(
    byte[] raw
) implements FLVPayload {

    @Override
    public boolean isSequenceHeader() {
        return false;
    }

    @Override
    public int size() {
        return this.raw.length;
    }

    @Override
    public void serialize(OutputStream out) throws IOException {
        out.write(this.raw);
    }

    @Override
    public final String toString() {
        return String.format(
            "FLVUnknownPayload[isSequenceHeader=?, size=%d]",
            this.size()
        );
    }

}
