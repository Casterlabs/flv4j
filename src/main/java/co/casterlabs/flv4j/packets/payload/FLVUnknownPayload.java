package co.casterlabs.flv4j.packets.payload;

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
    public final String toString() {
        return String.format(
            "FLVUnknownPayload[isSequenceHeader=?, size=%d]",
            this.size()
        );
    }

}
