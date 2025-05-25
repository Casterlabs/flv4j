package co.casterlabs.flv4j.flv.tags.video.data.avc;

import java.io.IOException;

import co.casterlabs.flv4j.actionscript.io.ASAssert;
import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASSizer;
import co.casterlabs.flv4j.actionscript.io.ASWriter;
import co.casterlabs.flv4j.flv.tags.video.data.VideoData;

// https://rtmp.veriskope.com/pdf/video_file_format_spec_v10.pdf#page=14
public record AVCVideoData(
    int rawType,
    int compositionTime,
    AVCVideoFrame frame
) implements VideoData {

    public AVCVideoData(int rawType, int compositionTime, AVCVideoFrame frame) {
        ASAssert.u8(rawType, "rawType");
        ASAssert.u24(compositionTime, "compositionTime");
        assert frame != null : "frame cannot be null";
        this.rawType = rawType;
        this.compositionTime = compositionTime;
        this.frame = frame;
    }

    public AVCVideoDataType type() {
        return AVCVideoDataType.LUT[this.rawType];
    }

    @Override
    public int size() {
        return new ASSizer()
            .u8()
            .u24()
            .bytes(this.frame.size()).size;
    }

    @Override
    public void serialize(ASWriter writer) throws IOException {
        writer.u8(this.rawType);
        writer.u24(this.compositionTime);
        this.frame.serialize(writer);
    }

    public static AVCVideoData parse(ASReader reader, int length) throws IOException {
        int rawType = reader.u8();
        int compositionTime = reader.u24();

        int frameLen = length - 4;
        AVCVideoFrame frame = switch (rawType) {
//            case 0 -> AVCDecoderConfigurationRecord.from(reader.limited(frameLen), frameLen); // TODO
            default -> new AVCVideoRawFrame(reader.bytes(frameLen));
        };

        return new AVCVideoData(
            rawType,
            compositionTime,
            frame
        );
    }

    @Override
    public final String toString() {
        return String.format(
            "AVCVideoData[type=%s (%d), frame=%s]",
            this.type(), this.rawType,
            this.frame
        );
    }

}
