package co.casterlabs.flv4j.flv.packets;

import java.io.IOException;

import co.casterlabs.flv4j.FLVSerializable;
import co.casterlabs.flv4j.actionscript.io.ASAssert;
import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASSizer;
import co.casterlabs.flv4j.actionscript.io.ASWriter;
import co.casterlabs.flv4j.flv.packets.payload.FLVPayload;
import co.casterlabs.flv4j.flv.packets.payload.FLVUnknownPayload;
import co.casterlabs.flv4j.flv.packets.payload.audio.FLVAudioPayload;
import co.casterlabs.flv4j.flv.packets.payload.script.FLVScriptPayload;
import co.casterlabs.flv4j.flv.packets.payload.video.FLVVideoPayload;

// https://en.wikipedia.org/wiki/Flash_Video#Flash_Video_Structure:~:text=newer%20expanded%20header-,Packets,-%5Bedit%5D
//https://rtmp.veriskope.com/pdf/video_file_format_spec_v10.pdf
public record FLVTag(
    FLVTagType type,
    long timestamp,
    int streamId,
    FLVPayload payload
) implements FLVSerializable {

    public FLVTag(FLVTagType type, long timestamp, int streamId, FLVPayload payload) {
        assert type != null : "type cannot be null";
        assert payload != null : "payload cannot be null";
        ASAssert.u32(timestamp, "timestamp");
        ASAssert.u24(streamId, "streamId");
        this.type = type;
        this.timestamp = timestamp;
        this.streamId = streamId;
        this.payload = payload;
    }

    @Override
    public int size() {
        return new ASSizer()
            .u8()
            .u24()
            .u24()
            .u8()
            .u24()
            .bytes(this.payload.size()).size;
    }

    @Override
    public void serialize(ASWriter writer) throws IOException {
        writer.u8(this.type.id);
        writer.u24(this.payload.size());

        writer.u24((int) this.timestamp & 0xFFFFFF);
        writer.u8((int) (this.timestamp >>> 24 & 0xFF)); // I hate this.

        writer.u24(this.streamId);
        this.payload.serialize(writer);
    }

    public static FLVTag parse(ASReader reader) throws IOException {
        int packetType = reader.u8();

        int payloadLen = reader.u24();

        int timestampL24 = reader.u24();
        int timestampH8 = reader.u8();
        long timestamp = (timestampH8 << 24) | timestampL24;

        int streamId = reader.u24();

        FLVPayload payload = switch (packetType) {
            case 8 -> FLVAudioPayload.parse(reader.limited(payloadLen), payloadLen);
            case 9 -> FLVVideoPayload.parse(reader.limited(payloadLen), payloadLen);
            case 18 -> FLVScriptPayload.parse(reader.limited(payloadLen));
            default -> new FLVUnknownPayload(reader.bytes(payloadLen));
        };

        return new FLVTag(
            FLVTagType.LUT[packetType],
            timestamp,
            streamId,
            payload
        );
    }

    @Override
    public final String toString() {
        return String.format(
            "FLVTag[packetType=%s, timestamp=%d, streamId=%d, payload=[%s], size=%d]",
            this.type,
            this.timestamp,
            this.streamId,
            this.payload,
            this.size()
        );
    }

}
