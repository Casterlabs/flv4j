package co.casterlabs.flv4j.packets;

import java.io.IOException;
import java.io.InputStream;

import co.casterlabs.commons.io.marshalling.PrimitiveMarshall;
import lombok.NonNull;

// https://en.wikipedia.org/wiki/Flash_Video#Flash_Video_Structure:~:text=%5Bedit%5D-,Header,-%5Bedit%5D
public record FLVFileHeader(
    int version,
    int flags,
    byte[] expandedHeaderData
) {

    public boolean isAudio() {
        return (this.flags & 0x04) != 0;
    }

    public boolean isVideo() {
        return (this.flags & 0x01) != 0;
    }

    public int size() {
        return 9 + this.expandedHeaderData.length;
    }

    @Override
    public final String toString() {
        StringBuilder expandedHeaderDataHex = new StringBuilder();
        for (byte b : this.expandedHeaderData) {
            expandedHeaderDataHex.append(" 0x");
            expandedHeaderDataHex.append(Integer.toString(b, 16));
        }

        return String.format(
            "FLVFileHeader[version=%d, flags=0x%x, isAudio=%b, isVideo=%b, expandedHeaderData=[%s], size=%d]",
            this.version,
            this.flags,
            this.isAudio(),
            this.isVideo(),
            expandedHeaderDataHex.isEmpty() ? "" : expandedHeaderDataHex.substring(1),
            this.size()
        );
    }

    public byte[] raw() {
        byte[] arr = new byte[9 + this.expandedHeaderData.length];
        arr[0] = 'F';
        arr[1] = 'L';
        arr[2] = 'V';
        arr[3] = (byte) this.version;
        arr[4] = (byte) this.flags;
        System.arraycopy(PrimitiveMarshall.BIG_ENDIAN.intToBytes(arr.length), 0, arr, 5, 4);
        System.arraycopy(this.expandedHeaderData, 0, arr, 9, this.expandedHeaderData.length);
        return arr;
    }

    public static FLVFileHeader from(@NonNull InputStream in) throws IOException {
        byte[] sig = in.readNBytes(3);

        if (sig[0] != 'F' || sig[1] != 'L' || sig[2] != 'V') {
            throw new IllegalArgumentException("Packet signature should be FLV, but is instead: " + new String(sig));
        }

        int version = in.read() & 0xFF;

        int flags = in.read() & 0xFF;

        int headerSize = PrimitiveMarshall.BIG_ENDIAN.bytesToInt(in.readNBytes(4));
        byte[] expandedHeaderData = in.readNBytes(9 - headerSize);

        return new FLVFileHeader(version, flags, expandedHeaderData);
    }

}
