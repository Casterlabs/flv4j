package co.casterlabs.flv4j.packets.payload.video;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.flv4j.packets.payload.FLVPayload;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

// https://rtmp.veriskope.com/pdf/video_file_format_spec_v10.pdf#page=13
// https://veovera.org/docs/enhanced/enhanced-rtmp-v1#defining-additional-video-codecs 
// https://veovera.org/docs/enhanced/enhanced-rtmp-v2#enhanced-video
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class FLVVideoPayload implements FLVPayload {

    private @Nullable byte[] raw;

    private final int rawFrameType;
    private final int rawCodec;
    private final byte[] data;

    public boolean isExHeader() {
        return (this.rawFrameType & 0b1000) != 0;
    }

    public FLVVideoFrameType frameType() {
        if (this.isExHeader()) {
            return FLVVideoFrameType.LUT[this.rawFrameType & 0b0111];
        } else {
            return FLVVideoFrameType.LUT[this.rawFrameType];
        }
    }

    public FLVVideoCodec codec() {
        return FLVVideoCodec.LUT[this.rawCodec];
    }

    @Override
    public final String toString() {
        return String.format(
            "FLVVideoPayload[frameType=%s (%d), codec=%s (%d), data=[len=%d], isSequenceHeader=%b]",
            this.frameType(),
            this.rawFrameType,
            this.codec(),
            this.rawCodec,
            this.data.length,
            this.isSequenceHeader()
        );
    }

    @Override
    public boolean isSequenceHeader() {
        if (this.isExHeader()) {
            return this.rawCodec == 0 || this.rawCodec == 4;
        } else {
            switch (this.codec()) {
                case H264:
                    return this.data[0] == 0;
                default:
                    return false; // TODO properly parse out the data.
            }
        }
    }

    @Override
    public byte[] raw() {
        if (this.raw == null) {
            // We don't have raw data cached. We need to create it.
            this.raw = new byte[this.size()];
            this.raw[0] = (byte) (this.rawFrameType << 4 | this.rawCodec);
            System.arraycopy(this.data, 0, this.raw, 1, this.data.length);
        }

        return this.raw;
    }

    @Override
    public int size() {
        return 1 + this.data.length;
    }

    @Override
    public FLVVideoPayload clone() {
        return new FLVVideoPayload(this.rawFrameType, this.rawCodec, this.data);
    }

    public static FLVVideoPayload from(byte[] raw) {
        byte firstByte = raw[0];

        byte frameType = (byte) ((firstByte >> 4) & 0b1111);
        byte codecId = (byte) (firstByte & 0b1111);

        byte[] data = new byte[raw.length - 1];
        System.arraycopy(raw, 1, data, 0, data.length);

        FLVVideoPayload payload = new FLVVideoPayload(
            frameType,
            codecId,
            data
        );
        payload.raw = raw;
        return payload;
    }

}
