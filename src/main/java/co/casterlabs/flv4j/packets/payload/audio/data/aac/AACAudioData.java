package co.casterlabs.flv4j.packets.payload.audio.data.aac;

import java.io.IOException;

import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASSizer;
import co.casterlabs.flv4j.actionscript.io.ASWriter;
import co.casterlabs.flv4j.packets.payload.audio.data.AudioData;

// https://rtmp.veriskope.com/pdf/video_file_format_spec_v10.pdf#page=12
public record AACAudioData(
    int rawType,
    AACAudioFrame frame
) implements AudioData {

    public AACAudioDataType type() {
        return AACAudioDataType.LUT[this.rawType];
    }

    @Override
    public int size() {
        return new ASSizer().u8()
            .bytes(this.frame.size()).size;
    }

    @Override
    public void serialize(ASWriter writer) throws IOException {
        writer.u8(this.rawType);
        this.frame.serialize(writer);
    }

    public static AACAudioData parse(ASReader reader, int length) throws IOException {
        int rawType = reader.u8();

        int frameLen = length - 1;
        AACAudioFrame frame = switch (rawType) {
//            case 0 -> AACAudioSpecificConfig.from(reader.limited(frameLen), frameLen); // TODO
            default -> new AACAudioRawFrame(reader.bytes(frameLen));
        };

        return new AACAudioData(
            rawType,
            frame
        );
    }

    @Override
    public final String toString() {
        return String.format(
            "AACAudioPayload[type=%s (%d), frame=%s]",
            this.type(), this.rawType,
            this.frame
        );
    }

}
