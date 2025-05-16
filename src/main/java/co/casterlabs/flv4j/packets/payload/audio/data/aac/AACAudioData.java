package co.casterlabs.flv4j.packets.payload.audio.data.aac;

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
        return 1 + this.frame.size();
    }

    @Override
    public byte[] raw() {
        byte[] raw = new byte[this.size()];
        raw[0] = (byte) this.rawType;
        System.arraycopy(this.frame.raw(), 0, raw, 1, this.frame.size());
        return raw;
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
