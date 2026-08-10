package co.casterlabs.flv4j.flv.tags.video;

import co.casterlabs.flv4j.actionscript.io.ASAssert;
import co.casterlabs.flv4j.actionscript.io.ASByteView;
import co.casterlabs.flv4j.codecs.VideoCodecData;
import co.casterlabs.flv4j.codecs.video.avc1.AVCVideoData;
import co.casterlabs.flv4j.codecs.video.hvc1.HEVCVideoData;

// https://veovera.org/docs/legacy/video-file-format-v10-0-spec.pdf#page=13
// https://veovera.org/docs/enhanced/enhanced-rtmp-v1#defining-additional-video-codecs 
// https://veovera.org/docs/enhanced/enhanced-rtmp-v2#enhanced-video
public record FLVStandardVideoTagData(
    ASByteView view,
    VideoCodecData data
) implements FLVVideoTagData {

    public FLVStandardVideoTagData(ASByteView view) {
        this(
            view,
            switch (view.u8(0) & 0b1111) {
                case 7 -> new AVCVideoData(view.slice(1));
                case 12 -> new HEVCVideoData(view.slice(1));
                default -> new VideoCodecData.Invalid(view.slice(1));
            }
        );
    }

    public static FLVStandardVideoTagData from(FLVVideoFrameType frameType, FLVVideoCodec codec, VideoCodecData data) {
        return from(frameType.id, codec.id, data);
    }

    public static FLVStandardVideoTagData from(int rawFrameType, int rawCodec, VideoCodecData codecData) {
        ASAssert.u4(rawFrameType, "rawFrameType");
        ASAssert.u4(rawCodec, "rawCodec");

        byte[] data = new byte[1 + codecData.view().length()];

        data[0] = (byte) ((rawFrameType << 4) | (rawCodec & 0b1111));

        System.arraycopy(codecData.view().buffer(), codecData.view().offset(), data, 1, codecData.view().length());

        return new FLVStandardVideoTagData(new ASByteView(data));
    }

    @Override
    public boolean isEx() {
        int rawFrameType = (this.view.u8(0) >> 4) & 0b1111;
        return (rawFrameType & 0b1000) != 0;
    }

    public FLVVideoFrameType frameType() {
        int rawFrameType = (this.view.u8(0) >> 4) & 0b1111;
        if (this.isEx()) {
            return FLVVideoFrameType.LUT[rawFrameType & 0b0111];
        } else {
            return FLVVideoFrameType.LUT[rawFrameType];
        }
    }

    public FLVVideoCodec codec() {
        int rawCodec = this.view.u8(0) & 0b1111;
        return FLVVideoCodec.LUT[rawCodec];
    }

    @Override
    public boolean isSequenceHeader() {
        if (this.isEx()) {
            int rawCodec = this.view.u8(0) & 0b1111;
            return rawCodec == 0 || rawCodec == 4;
        }
        return this.data.isSequenceHeader();
    }

    @Override
    public final String toString() {
        return String.format(
            "FLVVideoPayload[frameType=%s (%d), codec=%s (%d), isSequenceHeader=%b, data=%s]",
            this.frameType(), view.u8(0) >> 4 & 0b1111,
            this.codec(), view.u8(0) & 0b1111,
            this.isSequenceHeader(),
            this.data
        );
    }

}
