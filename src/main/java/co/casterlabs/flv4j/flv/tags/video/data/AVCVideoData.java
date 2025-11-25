package co.casterlabs.flv4j.flv.tags.video.data;

import java.io.IOException;

import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASSizer;
import co.casterlabs.flv4j.actionscript.io.ASWriter;
import lombok.AllArgsConstructor;

// https://rtmp.veriskope.com/pdf/video_file_format_spec_v10.pdf#page=14
public record AVCVideoData(
    int rawType,
    int compositionTimeOffset,
    byte[] data
) implements VideoData {

    public AVCPacketType type() {
        return AVCPacketType.LUT[this.rawType];
    }

    @Override
    public boolean isSequenceHeader() {
        return this.rawType == AVCPacketType.SEQUENCE_HEADER.id;
    }

    @Override
    public int size() {
        return ASSizer.u8 // rawType
            + ASSizer.u24 // compositionTimeOffset
            + this.data.length;
    }

    @Override
    public void serialize(ASWriter writer) throws IOException {
        writer.u8(this.rawType);
        writer.u24(this.compositionTimeOffset);
        writer.bytes(this.data);
    }

    public static AVCVideoData parse(ASReader reader, int length) throws IOException {
        int rawType = reader.u8();
        int compositionTimeOffset = reader.u24();
        byte[] data = reader.bytes(length - 4);

        return new AVCVideoData(rawType, compositionTimeOffset, data);
    }

    @Override
    public final String toString() {
        return String.format(
            "AVCVideoData[type=%s (%d), compositionTimeOffset=%d, data=[size=%d]]",
            AVCPacketType.LUT[this.rawType], this.rawType,
            this.compositionTimeOffset,
            this.data.length
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
