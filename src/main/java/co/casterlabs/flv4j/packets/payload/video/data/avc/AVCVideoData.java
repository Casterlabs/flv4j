package co.casterlabs.flv4j.packets.payload.video.data.avc;

import java.io.IOException;
import java.io.OutputStream;

import co.casterlabs.flv4j.packets.payload.video.data.VideoData;
import co.casterlabs.flv4j.util.ASReader;
import co.casterlabs.flv4j.util.ASSizer;
import co.casterlabs.flv4j.util.ASWriter;

// https://rtmp.veriskope.com/pdf/video_file_format_spec_v10.pdf#page=14
public record AVCVideoData(
    int rawType,
    int compositionTime,
    AVCVideoFrame frame
) implements VideoData {

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
    public void serialize(OutputStream out) throws IOException {
        ASWriter.u8(out, this.rawType);
        ASWriter.u24(out, this.compositionTime);
        this.frame.serialize(out);
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
