package co.casterlabs.flv4j.codecs.video.avc1;

import co.casterlabs.flv4j.actionscript.io.ASAssert;
import co.casterlabs.flv4j.actionscript.io.ASByteView;
import co.casterlabs.flv4j.codecs.VideoCodecData;

// https://veovera.org/docs/legacy/video-file-format-v10-0-spec.pdf#page=14
public record AVCVideoData(
    ASByteView view,
    AVCPacketData packetData
) implements VideoCodecData {

    public AVCVideoData(ASByteView view) {
        this(
            view,
            switch (AVCPacketType.LUT[view.u8(0)]) {
                case SEQUENCE_HEADER -> new AVCDecoderConfigurationRecord(view.slice(4));
                case NALU -> new AVCNalus(view.slice(4));
                default -> new AVCPacketData.Invalid(view.slice(4));
            }
        );
    }

    public static AVCVideoData from(AVCPacketType type, int compositionTimeOffset, AVCPacketData packetData) {
        return from(type.id, compositionTimeOffset, packetData);
    }

    public static AVCVideoData from(int rawType, int compositionTimeOffset, AVCPacketData packetData) {
        ASAssert.u8(rawType, "rawType");
        ASAssert.u24(compositionTimeOffset, "compositionTimeOffset");

        byte[] data = new byte[4 + packetData.view().length()];

        data[0] = (byte) (rawType & 0xFF);
        data[1] = (byte) ((compositionTimeOffset >> 16) & 0xFF);
        data[2] = (byte) ((compositionTimeOffset >> 8) & 0xFF);
        data[3] = (byte) ((compositionTimeOffset >> 0) & 0xFF);

        System.arraycopy(packetData.view().buffer(), packetData.view().offset(), data, 4, packetData.view().length());

        return new AVCVideoData(new ASByteView(data));
    }

    public AVCPacketType type() {
        int rawType = this.view.u8(0);
        return AVCPacketType.LUT[rawType];
    }

    @Override
    public boolean isSequenceHeader() {
        int rawType = this.view.u8(0);
        return rawType == AVCPacketType.SEQUENCE_HEADER.id;
    }

    @Override
    public int compositionTimeOffset() {
        return this.view.u24(1);
    }

    @Override
    public String toString() {
        return String.format(
            "AVCVideoData[type=%s (%d), compositionTimeOffset=%d, data=%s]",
            this.type(), this.view.u8(0),
            this.compositionTimeOffset(),
            this.packetData
        );
    }

}
