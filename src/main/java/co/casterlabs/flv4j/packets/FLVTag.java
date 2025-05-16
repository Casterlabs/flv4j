package co.casterlabs.flv4j.packets;

import java.io.IOException;
import java.io.InputStream;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.commons.io.marshalling.PrimitiveMarshall;
import co.casterlabs.flv4j.FLVSerializable;
import co.casterlabs.flv4j.packets.payload.FLVPayload;
import co.casterlabs.flv4j.packets.payload.FLVUnknownPayload;
import co.casterlabs.flv4j.packets.payload.audio.FLVAudioPayload;
import co.casterlabs.flv4j.packets.payload.video.FLVVideoPayload;
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
        return 11 + this.payload.size();
    }

    @Override
    public byte[] raw() {
        byte[] raw = new byte[this.size()];

        raw[0] = (@Nullable byte) this.type.id;
        System.arraycopy(PrimitiveMarshall.BIG_ENDIAN.intToBytes(this.payloadSize), 1, raw, 1, 3);

        byte[] timestampBytes = PrimitiveMarshall.BIG_ENDIAN.longToBytes(this.timestamp);
        System.arraycopy(timestampBytes, 5, raw, 4, 3);
        raw[7] = timestampBytes[4];

        System.arraycopy(PrimitiveMarshall.BIG_ENDIAN.intToBytes(this.streamId), 1, raw, 8, 3);

        System.arraycopy(this.payload.raw(), 0, raw, 11, this.payload.size());

        return raw;
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
//            case 18 -> new FLVScriptTag(payloadBytes); // TODO
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
