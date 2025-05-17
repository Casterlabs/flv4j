package co.casterlabs.flv4j.packets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import co.casterlabs.commons.io.marshalling.PrimitiveMarshall;
import co.casterlabs.flv4j.FLVSerializable;
import co.casterlabs.flv4j.packets.payload.FLVPayload;
import co.casterlabs.flv4j.packets.payload.FLVUnknownPayload;
import co.casterlabs.flv4j.packets.payload.audio.FLVAudioPayload;
import co.casterlabs.flv4j.packets.payload.script.FLVScriptPayload;
import co.casterlabs.flv4j.packets.payload.video.FLVVideoPayload;
import co.casterlabs.flv4j.util.ASSizer;
import co.casterlabs.flv4j.util.ASWriter;
import lombok.NonNull;

// https://en.wikipedia.org/wiki/Flash_Video#Flash_Video_Structure:~:text=newer%20expanded%20header-,Packets,-%5Bedit%5D
//https://rtmp.veriskope.com/pdf/video_file_format_spec_v10.pdf
public record FLVTag(
    FLVTagType type,
    int payloadSize,
    long timestamp,
    int streamId,
    FLVPayload payload
) implements FLVSerializable {

    @Override
    public int size() {
        return new ASSizer()
            .u8()
            .u24()
            .u24()
            .u8()
            .u24()
            .bytes(payloadSize).size;
    }

    @Override
    public void serialize(OutputStream out) throws IOException {
        ASWriter.u8(out, this.type.id);
        ASWriter.u24(out, this.payloadSize);

//        byte[] timestampBytes = PrimitiveMarshall.BIG_ENDIAN.longToBytes(this.timestamp);
//        out.write(timestampBytes, 5, 3);
//        ASWriter.u8(out, timestampBytes[4]);
        ASWriter.u24(out, (int) this.timestamp & 0xFFFFFF);
        ASWriter.u8(out, (int) (this.timestamp >>> 24 & 0xFF)); // I hate this.

        ASWriter.u24(out, this.streamId);
        this.payload.serialize(out);
    }

    public static FLVTag from(@NonNull InputStream in) throws IOException {
        byte[] headerBytes = in.readNBytes(11);

        int payloadLen = PrimitiveMarshall.BIG_ENDIAN.bytesToInt(new byte[] {
                0,
                headerBytes[1],
                headerBytes[2],
                headerBytes[3]
        });
        byte[] payloadBytes = in.readNBytes(payloadLen);

        byte packetType = headerBytes[0];

        int payloadSize = PrimitiveMarshall.BIG_ENDIAN.bytesToInt(new byte[] {
                0,
                headerBytes[1],
                headerBytes[2],
                headerBytes[3]
        });

        long timestamp = PrimitiveMarshall.BIG_ENDIAN.bytesToLong(new byte[] {
                0,
                0,
                0,
                0,
                headerBytes[7],
                headerBytes[4],
                headerBytes[5],
                headerBytes[6],
        });

        int streamId = PrimitiveMarshall.BIG_ENDIAN.bytesToInt(new byte[] {
                0,
                headerBytes[8],
                headerBytes[9],
                headerBytes[10]
        });

        FLVPayload payload = switch (packetType) {
            case 8 -> FLVAudioPayload.from(payloadBytes);
            case 9 -> FLVVideoPayload.from(payloadBytes);
            case 18 -> FLVScriptPayload.from(payloadBytes);
            default -> new FLVUnknownPayload(payloadBytes);
        };

        return new FLVTag(
            FLVTagType.LUT[packetType],
            payloadSize,
            timestamp,
            streamId,
            payload
        );
    }

    @Override
    public final String toString() {
        return String.format(
            "FLVTag[packetType=%s, payloadSize=%d, timestamp=%d, streamId=%d, payload=[%s], size=%d]",
            this.type,
            this.payloadSize,
            this.timestamp,
            this.streamId,
            this.payload,
            this.size()
        );
    }

}
