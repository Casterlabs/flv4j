package co.casterlabs.flv4j.flv.packets;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import co.casterlabs.flv4j.FLVSerializable;
import co.casterlabs.flv4j.actionscript.io.ASAssert;
import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASSizer;
import co.casterlabs.flv4j.actionscript.io.ASWriter;

// https://en.wikipedia.org/wiki/Flash_Video#Flash_Video_Structure:~:text=%5Bedit%5D-,Header,-%5Bedit%5D
public record FLVFileHeader(
    int version,
    int flags,
    byte[] expandedHeaderData
) implements FLVSerializable {

    private static final byte[] MAGIC = "FLV".getBytes(StandardCharsets.US_ASCII);

    public FLVFileHeader(int version, int flags, byte[] expandedHeaderData) {
        ASAssert.u8(version, "version");
        ASAssert.u8(flags, "flags");
        this.version = version;
        this.flags = flags;
        this.expandedHeaderData = expandedHeaderData;
    }

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
    public void serialize(ASWriter writer) throws IOException {
        writer.bytes(MAGIC);
        writer.u8(this.version);
        writer.u8(this.flags);
        writer.u32(this.size()); // [sic]
        writer.bytes(this.expandedHeaderData);
    }

    public static FLVFileHeader parse(ASReader reader) throws IOException {
        byte[] sig = reader.bytes(MAGIC.length);
        if (!Arrays.equals(sig, MAGIC)) {
            throw new IllegalArgumentException("Packet signature should be FLV, but is instead: " + new String(sig));
        }

        int version = reader.u8();
        int flags = reader.u8();

        int headerSize = (int) reader.u32();
        byte[] expandedHeaderData = reader.bytes(9 - headerSize);

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
