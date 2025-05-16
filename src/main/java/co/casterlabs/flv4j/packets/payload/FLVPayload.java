package co.casterlabs.flv4j.packets.payload;

public interface FLVPayload {

    public boolean isSequenceHeader();

    public int size();

    public byte[] raw();

}
