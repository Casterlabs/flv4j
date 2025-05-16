package co.casterlabs.flv4j.packets.payload.video.data;

public record UnknownVideoData(
    byte[] raw
) implements VideoData {

    @Override
    public int size() {
        return this.raw.length;
    }

    @Override
    public final String toString() {
        return String.format(
            "UnknownVideoData[size=%d]",
            this.size()
        );
    }

}
