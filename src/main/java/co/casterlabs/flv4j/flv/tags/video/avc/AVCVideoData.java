package co.casterlabs.flv4j.flv.tags.video.avc;

import co.casterlabs.flv4j.actionscript.io.ASByteView;
import co.casterlabs.flv4j.flv.tags.video.VideoData;
import lombok.AllArgsConstructor;

// https://rtmp.veriskope.com/pdf/video_file_format_spec_v10.pdf#page=14
public record AVCVideoData(
    ASByteView view,
    AVCPacketData packetData
) implements VideoData {

    public AVCVideoData(ASByteView view) {
        this(
            view,
            switch (AVCPacketType.LUT[view.u8(0)]) {
                case SEQUENCE_HEADER -> new AVCDecoderConfigurationRecord(view.slice(4));
                case NALU -> new AVCNalus(view.slice(4));
                default -> AVCPacketData.INVALID;
            }
        );
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

    @AllArgsConstructor
    public static enum AVCPacketType {
        SEQUENCE_HEADER(0),
        NALU(1),
        END_OF_SEQUENCE(2),
        ;

        public static final AVCPacketType[] LUT = new AVCPacketType[255];
        static {
            for (AVCPacketType e : values()) {
                LUT[e.id] = e;
            }
        }

        public final int id;

    }

}
