package co.casterlabs.flv4j.packets.payload.video.data.avc;

import java.io.IOException;
import java.io.OutputStream;

import co.casterlabs.commons.io.marshalling.PrimitiveMarshall;
import co.casterlabs.flv4j.packets.payload.video.data.VideoData;
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

    public static AVCVideoData from(byte[] raw) {
        byte rawType = raw[0];
        int compositionTime = PrimitiveMarshall.BIG_ENDIAN.bytesToInt(new byte[] {
                0,
                raw[1],
                raw[2],
                raw[3]
        });

        byte[] frameBytes = new byte[raw.length - 4];
        System.arraycopy(raw, 4, frameBytes, 0, frameBytes.length);

        AVCVideoFrame frame = switch (rawType) {
//            case 0 -> AVCDecoderConfigurationRecord.from(frameBytes); // TODO
            default -> new AVCVideoRawFrame(frameBytes);
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
