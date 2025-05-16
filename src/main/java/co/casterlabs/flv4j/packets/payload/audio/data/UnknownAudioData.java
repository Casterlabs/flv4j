package co.casterlabs.flv4j.packets.payload.audio.data;

public record UnknownAudioData(
    byte[] raw
) implements AudioData {

    @Override
    public int size() {
        return this.raw.length;
    }

    @Override
    public final String toString() {
        return String.format(
            "UnknownAudioData[size=%d]",
            this.size()
        );
    }

}
