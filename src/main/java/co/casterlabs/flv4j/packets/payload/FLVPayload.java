package co.casterlabs.flv4j.packets.payload;

public interface FLVPayload {

    public boolean isSequenceHeader();

    public byte[] raw();

    public int size();

    public FLVPayload clone();

}
