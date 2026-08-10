package co.casterlabs.flv4j.flv.tags;

import java.io.IOException;

import co.casterlabs.flv4j.FLVSerializable;
import co.casterlabs.flv4j.actionscript.io.ASAssert;
import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASSizer;
import co.casterlabs.flv4j.actionscript.io.ASWriter;
import co.casterlabs.flv4j.flv.tags.audio.FLVAudioTagData;
import co.casterlabs.flv4j.flv.tags.script.FLVScriptTagData;
import co.casterlabs.flv4j.flv.tags.video.FLVVideoTagData;

// https://en.wikipedia.org/wiki/Flash_Video#Flash_Video_Structure:~:text=newer%20expanded%20header-,Packets,-%5Bedit%5D
//https://veovera.org/docs/legacy/video-file-format-v10-0-spec.pdf
public record FLVTag(
    int rawType,
    long timestamp,
    int streamId,
    FLVTagData data
) implements FLVSerializable {

    public FLVTag(FLVTagType type, long timestamp, int streamId, FLVTagData data) {
        this(type.id, timestamp, streamId, data);
    }

    public FLVTag(int rawType, long timestamp, int streamId, FLVTagData data) {
        assert data != null : "data cannot be null";
        ASAssert.u8(rawType, "rawType");
        ASAssert.u32(timestamp, "timestamp");
        ASAssert.u24(streamId, "streamId");
        this.rawType = rawType;
        this.timestamp = timestamp;
        this.streamId = streamId;
        this.data = data;
    }

    public FLVTagType type() {
        return FLVTagType.LUT[this.rawType];
    }

    @Override
    public int size() {
        return ASSizer.u8 // type
            + ASSizer.u24 // data length
            + ASSizer.u24 // timestamp
            + ASSizer.u8  // timestamp extended
            + ASSizer.u24 // stream id
            + this.data.size();
    }

    @Override
    public void serialize(ASWriter writer) throws IOException {
        writer.u8(this.rawType);
        writer.u24(this.data.size());

        writer.u24((int) this.timestamp & 0xFFFFFF);
        writer.u8((int) (this.timestamp >>> 24 & 0xFF)); // I hate this.

        writer.u24(this.streamId);
        this.data.serialize(writer);
    }

    public static FLVTag parse(ASReader reader) throws IOException {
        int packetType = reader.u8();

        int dataLen = reader.u24();

        int timestampL24 = reader.u24();
        int timestampH8 = reader.u8();
        long timestamp = ((long) timestampH8 << 24) | timestampL24;

        int streamId = reader.u24();

        FLVTagData data = switch (packetType) {
            case 8 -> FLVAudioTagData.parse(reader.limited(dataLen), dataLen);
            case 9 -> FLVVideoTagData.parse(reader.limited(dataLen), dataLen);
            case 18 -> FLVScriptTagData.parse(reader.limited(dataLen));
            default -> new FLVTagHeaderUnknown(reader.bytes(dataLen));
        };

        return new FLVTag(
            packetType,
            timestamp,
            streamId,
            data
        );
    }

    @Override
    public final String toString() {
        return String.format(
            "FLVTag[packetType=%s (%d), timestamp=%d, streamId=%d, data=[%s], size=%d]",
            this.type(), this.rawType,
            this.timestamp,
            this.streamId,
            this.data,
            this.size()
        );
    }

}
