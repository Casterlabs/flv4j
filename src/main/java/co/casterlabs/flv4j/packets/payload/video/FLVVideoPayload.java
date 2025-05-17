package co.casterlabs.flv4j.packets.payload.video;

import java.io.IOException;
import java.io.OutputStream;

import co.casterlabs.flv4j.packets.payload.FLVPayload;
import co.casterlabs.flv4j.packets.payload.video.data.UnknownVideoData;
import co.casterlabs.flv4j.packets.payload.video.data.VideoData;
import co.casterlabs.flv4j.packets.payload.video.data.avc.AVCVideoData;
import co.casterlabs.flv4j.util.ASSizer;
import co.casterlabs.flv4j.util.ASWriter;

// https://rtmp.veriskope.com/pdf/video_file_format_spec_v10.pdf#page=13
// https://veovera.org/docs/enhanced/enhanced-rtmp-v1#defining-additional-video-codecs 
// https://veovera.org/docs/enhanced/enhanced-rtmp-v2#enhanced-video
public record FLVVideoPayload(
    int rawFrameType,
    int rawCodec,
    VideoData data
) implements FLVPayload {

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
    public boolean isSequenceHeader() {
        if (this.isExHeader()) {
            return this.rawCodec == 0 || this.rawCodec == 4;
        }

        return switch (this.codec()) {
            case H264 -> ((AVCVideoData) this.data).rawType() == 0;
            default -> false; // TODO properly parse out the data.
        };
    }

    @Override
    public int size() {
        return new ASSizer().u8()
            .bytes(this.data.size()).size;
    }

    @Override
    public void serialize(OutputStream out) throws IOException {
        int fb = this.rawFrameType << 4 | this.rawCodec;
        ASWriter.u8(out, fb);
        this.data.serialize(out);
    }

    public static FLVVideoPayload from(byte[] raw) {
        byte firstByte = raw[0];

        byte frameType = (byte) ((firstByte >> 4) & 0b1111);
        byte codecId = (byte) (firstByte & 0b1111);

        byte[] dataBytes = new byte[raw.length - 1];
        System.arraycopy(raw, 1, dataBytes, 0, dataBytes.length);

        VideoData data = switch (codecId) {
            case 7 -> AVCVideoData.from(dataBytes);
            default -> new UnknownVideoData(dataBytes);
        };

        return new FLVVideoPayload(
            frameType,
            codecId,
            data
        );
    }

    @Override
    public final String toString() {
        return String.format(
            "FLVVideoPayload[frameType=%s (%d), codec=%s (%d), data=%s, isSequenceHeader=%b]",
            this.frameType(), this.rawFrameType,
            this.codec(), this.rawCodec,
            this.data,
            this.isSequenceHeader()
        );
    }

}
