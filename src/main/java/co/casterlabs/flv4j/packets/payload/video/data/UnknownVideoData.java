package co.casterlabs.flv4j.packets.payload.video.data;

import java.io.IOException;
import java.io.OutputStream;

public record UnknownVideoData(
    byte[] raw
) implements VideoData {

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
            "UnknownVideoData[size=%d]",
            this.size()
        );
    }

}
