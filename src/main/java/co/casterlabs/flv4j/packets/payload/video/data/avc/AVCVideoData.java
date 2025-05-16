package co.casterlabs.flv4j.packets.payload.video.data.avc;

import co.casterlabs.commons.io.marshalling.PrimitiveMarshall;
import co.casterlabs.flv4j.packets.payload.video.data.VideoData;

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
        return 4 + this.frame.size();
    }

    @Override
    public byte[] raw() {
        byte[] raw = new byte[this.size()];
        raw[0] = (byte) this.rawType;
        System.arraycopy(PrimitiveMarshall.BIG_ENDIAN.intToBytes(this.compositionTime), 1, raw, 1, 3);
        System.arraycopy(this.frame.raw(), 0, raw, 4, this.frame.size());
        return raw;
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
