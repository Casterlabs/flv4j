package co.casterlabs.flv4j.flv.tags;

import java.io.IOException;

import co.casterlabs.flv4j.FLVSerializable;
import co.casterlabs.flv4j.actionscript.io.ASAssert;
import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASSizer;
import co.casterlabs.flv4j.actionscript.io.ASWriter;
import co.casterlabs.flv4j.flv.tags.audio.FLVAudioTagData;
import co.casterlabs.flv4j.flv.tags.script.FLVScriptTagData;
import co.casterlabs.flv4j.flv.tags.video.FLVVideoPayload;

// https://en.wikipedia.org/wiki/Flash_Video#Flash_Video_Structure:~:text=newer%20expanded%20header-,Packets,-%5Bedit%5D
//https://rtmp.veriskope.com/pdf/video_file_format_spec_v10.pdf
public record FLVTag(
    FLVTagType type,
    long timestamp,
    int streamId,
    FLVTagData data
) implements FLVSerializable {

    public FLVTag(FLVTagType type, long timestamp, int streamId, FLVTagData data) {
        assert type != null : "type cannot be null";
        assert data != null : "data cannot be null";
        ASAssert.u32(timestamp, "timestamp");
        ASAssert.u24(streamId, "streamId");
        this.type = type;
        this.timestamp = timestamp;
        this.streamId = streamId;
        this.data = data;
    }

    @Override
    public int size() {
        return new ASSizer()
            .u8()
            .u24()
            .u24()
            .u8()
            .u24()
            .bytes(this.data.size()).size;
    }

    @Override
    public void serialize(ASWriter writer) throws IOException {
        writer.u8(this.type.id);
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
        long timestamp = (timestampH8 << 24) | timestampL24;

        int streamId = reader.u24();

        FLVTagData data = switch (packetType) {
            case 8 -> FLVAudioTagData.parse(reader.limited(dataLen), dataLen);
            case 9 -> FLVVideoPayload.parse(reader.limited(dataLen), dataLen);
            case 18 -> FLVScriptTagData.parse(reader.limited(dataLen));
            default -> new FLVTagHeaderUnknown(reader.bytes(dataLen));
        };

        return new FLVTag(
            FLVTagType.LUT[packetType],
            timestamp,
            streamId,
            data
        );
    }

    @Override
    public final String toString() {
        return String.format(
            "FLVTag[packetType=%s, timestamp=%d, streamId=%d, data=[%s], size=%d]",
            this.type,
            this.timestamp,
            this.streamId,
            this.data,
            this.size()
        );
    }

}
