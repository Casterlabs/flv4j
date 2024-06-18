package co.casterlabs.flv4j.packets;

import java.io.IOException;
import java.io.InputStream;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.commons.io.marshalling.PrimitiveMarshall;
import co.casterlabs.flv4j.packets.payload.FLVPayload;
import co.casterlabs.flv4j.packets.payload.FLVUnknownPayload;
import co.casterlabs.flv4j.packets.payload.audio.FLVAudioPayload;
import co.casterlabs.flv4j.packets.payload.video.FLVVideoPayload;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

// https://en.wikipedia.org/wiki/Flash_Video#Flash_Video_Structure:~:text=newer%20expanded%20header-,Packets,-%5Bedit%5D
//https://rtmp.veriskope.com/pdf/video_file_format_spec_v10.pdf
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class FLVTag {

    private @Nullable byte[] raw;

    private final FLVTagType type;
    private final int payloadSize;
    private final long timestamp;
    private final int streamId;
    private final FLVPayload payload;

    public int size() {
        return 11 + this.payload.size();
    }

    @Override
    public final String toString() {
        return String.format(
            "FLVTag[packetType=%s, payloadSize=%d, timestamp=%d, streamId=%d, payloadData=[%s], size=%d]",
            this.type,
            this.payloadSize,
            this.timestamp,
            this.streamId,
            this.payload,
            this.size()
        );
    }

    public byte[] raw() {
        if (this.raw == null) {
            // We don't have raw data cached. We need to create it.
            this.raw = new byte[this.size()];

            this.raw[0] = (@Nullable byte) this.type.id;
            System.arraycopy(PrimitiveMarshall.BIG_ENDIAN.intToBytes(this.payloadSize), 1, this.raw, 1, 3);

            byte[] timestampBytes = PrimitiveMarshall.BIG_ENDIAN.longToBytes(this.timestamp);
            System.arraycopy(timestampBytes, 5, this.raw, 4, 3);
            this.raw[7] = timestampBytes[4];

            System.arraycopy(PrimitiveMarshall.BIG_ENDIAN.intToBytes(this.streamId), 1, this.raw, 8, 3);

            System.arraycopy(this.payload.raw(), 0, this.raw, 11, this.payload.size());
        }

        return this.raw;
    }

    public static FLVTag from(@NonNull InputStream in) throws IOException {
        byte[] base = in.readNBytes(11);
        byte[] payload = in.readNBytes(PrimitiveMarshall.BIG_ENDIAN.bytesToInt(new byte[] {
                0,
                base[1],
                base[2],
                base[3]
        }));

        byte[] raw = new byte[base.length + payload.length];
        System.arraycopy(base, 0, raw, 0, base.length);
        System.arraycopy(payload, 0, raw, base.length, payload.length);

        return from(raw);
    }

    public static FLVTag from(byte[] raw) {
        byte packetType = raw[0];

        int payloadSize = PrimitiveMarshall.BIG_ENDIAN.bytesToInt(new byte[] {
                0,
                raw[1],
                raw[2],
                raw[3]
        });

        long timestamp = PrimitiveMarshall.BIG_ENDIAN.bytesToLong(new byte[] {
                0,
                0,
                0,
                0,
                raw[7],
                raw[4],
                raw[5],
                raw[6],
        });

        int streamId = PrimitiveMarshall.BIG_ENDIAN.bytesToInt(new byte[] {
                0,
                raw[8],
                raw[9],
                raw[10]
        });

        byte[] payloadData = new byte[payloadSize];
        System.arraycopy(raw, 11, payloadData, 0, payloadSize);

        FLVPayload payload = null;

        switch (packetType) {
            case 8:
                payload = FLVAudioPayload.from(payloadData);
                break;
            case 9:
                payload = FLVVideoPayload.from(payloadData);
                break;
            case 18:
                payload = new FLVUnknownPayload(payloadData); // TODO
                break;
            default:
                payload = new FLVUnknownPayload(payloadData);
                break;
        }

        FLVTag tag = new FLVTag(
            FLVTagType.LUT[packetType],
            payloadSize,
            timestamp,
            streamId,
            payload
        );
        tag.raw = raw;
        return tag;
    }

}
