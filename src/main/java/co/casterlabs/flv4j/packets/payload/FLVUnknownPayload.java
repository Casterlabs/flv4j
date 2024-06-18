package co.casterlabs.flv4j.packets.payload;

public record FLVUnknownPayload(
    byte[] raw
) implements FLVPayload {

    @Override
    public final String toString() {
        return "FLVUnknownPayload[isSequenceHeader=false]";
    }

    @Override
    public boolean isSequenceHeader() {
        return false;
    }

    @Override
    public int size() {
        return this.raw.length;
    }

    @Override
    public FLVUnknownPayload clone() {
        return new FLVUnknownPayload(this.raw);
    }

}
