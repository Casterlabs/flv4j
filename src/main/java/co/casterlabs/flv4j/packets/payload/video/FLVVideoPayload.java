package co.casterlabs.flv4j.packets.payload.video;

import java.io.IOException;

import co.casterlabs.flv4j.actionscript.io.ASAssert;
import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASSizer;
import co.casterlabs.flv4j.actionscript.io.ASWriter;
import co.casterlabs.flv4j.packets.payload.FLVPayload;
import co.casterlabs.flv4j.packets.payload.video.data.UnknownVideoData;
import co.casterlabs.flv4j.packets.payload.video.data.VideoData;
import co.casterlabs.flv4j.packets.payload.video.data.avc.AVCVideoData;

// https://rtmp.veriskope.com/pdf/video_file_format_spec_v10.pdf#page=13
// https://veovera.org/docs/enhanced/enhanced-rtmp-v1#defining-additional-video-codecs 
// https://veovera.org/docs/enhanced/enhanced-rtmp-v2#enhanced-video
public record FLVVideoPayload(
    int rawFrameType,
    int rawCodec,
    VideoData data
) implements FLVPayload {

    public FLVVideoPayload(int rawFrameType, int rawCodec, VideoData data) {
        ASAssert.u4(rawFrameType, "rawFrameType");
        ASAssert.u4(rawCodec, "rawCodec");
        assert data != null : "data cannot be null";
        this.rawFrameType = rawFrameType;
        this.rawCodec = rawCodec;
        this.data = data;
    }

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
    public void serialize(ASWriter writer) throws IOException {
        int fb = this.rawFrameType << 4 | this.rawCodec;
        writer.u8(fb);
        this.data.serialize(writer);
    }

    public static FLVVideoPayload parse(ASReader reader, int length) throws IOException {
        int fb = reader.u8();

        int frameType = fb >> 4 & 0b1111;
        int codecId = fb & 0b1111;

        int dataLen = length - 1;
        VideoData data = switch (codecId) {
            case 7 -> AVCVideoData.parse(reader.limited(dataLen), dataLen);
            default -> new UnknownVideoData(reader.bytes(dataLen));
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
