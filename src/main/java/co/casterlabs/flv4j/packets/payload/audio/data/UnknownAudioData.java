package co.casterlabs.flv4j.packets.payload.audio.data;

import java.io.IOException;
import java.io.OutputStream;

public record UnknownAudioData(
    byte[] raw
) implements AudioData {

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
            "UnknownAudioData[size=%d]",
            this.size()
        );
    }

}
