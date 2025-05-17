package co.casterlabs.flv4j.packets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import co.casterlabs.commons.io.marshalling.PrimitiveMarshall;
import co.casterlabs.flv4j.FLVSerializable;
import co.casterlabs.flv4j.util.ASSizer;
import co.casterlabs.flv4j.util.ASWriter;
import lombok.NonNull;

// https://en.wikipedia.org/wiki/Flash_Video#Flash_Video_Structure:~:text=%5Bedit%5D-,Header,-%5Bedit%5D
public record FLVFileHeader(
    int version,
    int flags,
    byte[] expandedHeaderData
) implements FLVSerializable {

    private static final byte[] MAGIC = "FLV".getBytes(StandardCharsets.US_ASCII);

    public boolean isAudio() {
        return (this.flags & 0x04) != 0;
    }

    public boolean isVideo() {
        return (this.flags & 0x01) != 0;
    }

    @Override
    public int size() {
        return new ASSizer()
            .bytes(MAGIC.length)
            .u8()
            .u8()
            .u32()
            .bytes(this.expandedHeaderData.length).size;
    }

    @Override
    public void serialize(OutputStream out) throws IOException {
        out.write(MAGIC);
        ASWriter.u8(out, this.version);
        ASWriter.u8(out, this.flags);
        ASWriter.u32(out, this.size()); // [sic]
        out.write(this.expandedHeaderData);
    }

    public static FLVFileHeader from(@NonNull InputStream in) throws IOException {
        byte[] sig = in.readNBytes(MAGIC.length);
        if (!Arrays.equals(sig, MAGIC)) {
            throw new IllegalArgumentException("Packet signature should be FLV, but is instead: " + new String(sig));
        }

        int version = in.read() & 0xFF;

        int flags = in.read() & 0xFF;

        int headerSize = PrimitiveMarshall.BIG_ENDIAN.bytesToInt(in.readNBytes(4));
        byte[] expandedHeaderData = in.readNBytes(9 - headerSize);

        return new FLVFileHeader(version, flags, expandedHeaderData);
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

}
