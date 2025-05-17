package co.casterlabs.flv4j.packets.payload.audio.data.aac;

import java.io.IOException;
import java.io.OutputStream;

import co.casterlabs.flv4j.packets.payload.audio.data.AudioData;
import co.casterlabs.flv4j.util.ASSizer;
import co.casterlabs.flv4j.util.ASWriter;

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
    public void serialize(OutputStream out) throws IOException {
        ASWriter.u8(out, this.rawType);
        this.frame.serialize(out);
    }

    public static AACAudioData from(byte[] raw) {
        byte rawType = raw[0];

        byte[] frameBytes = new byte[raw.length - 1];
        System.arraycopy(raw, 1, frameBytes, 0, frameBytes.length);

        AACAudioFrame frame = switch (rawType) {
//            case 0 -> AACAudioSpecificConfig.from(frameBytes); // TODO
            default -> new AACAudioRawFrame(frameBytes);
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
