package co.casterlabs.flv4j.packets.payload.audio;

import co.casterlabs.flv4j.packets.payload.FLVPayload;
import co.casterlabs.flv4j.packets.payload.audio.data.AudioData;
import co.casterlabs.flv4j.packets.payload.audio.data.UnknownAudioData;
import co.casterlabs.flv4j.packets.payload.audio.data.aac.AACAudioData;

// https://rtmp.veriskope.com/pdf/video_file_format_spec_v10.pdf#page=10
// https://veovera.org/docs/enhanced/enhanced-rtmp-v2#enhanced-audio
public record FLVAudioPayload(
    int rawFormat,
    int rawRate,
    int rawSampleSize,
    int rawChannels,
    AudioData data
) implements FLVPayload {

    public boolean isExHeader() {
        return this.rawFormat == 9;
    }

    public FLVAudioFormat format() {
        return FLVAudioFormat.LUT[this.rawFormat];
    }

    public FLVAudioRate rate() {
        return FLVAudioRate.LUT[this.rawRate];
    }

    public FLVAudioSampleSize sampleSize() {
        return FLVAudioSampleSize.LUT[this.rawSampleSize];
    }

    public FLVAudioChannels channels() {
        return FLVAudioChannels.LUT[this.rawChannels];
    }

    @Override
    public boolean isSequenceHeader() {
        if (this.isExHeader()) {
            byte packetType = (byte) (this.rawRate << 2 | this.rawSampleSize << 1 | this.rawChannels); // Reconstruct the lower-4 bits of the first byte.
            return packetType == 0;
        }

        return switch (this.format()) {
            case AAC -> ((AACAudioData) this.data).rawType() == 0;
            default -> false; // TODO properly parse out the data.
        };
    }

    @Override
    public int size() {
        return 1 + this.data.size();
    }

    @Override
    public byte[] raw() {
        byte[] raw = new byte[this.size()];
        raw[0] = (byte) (this.rawFormat << 4 | this.rawRate << 2 | this.rawSampleSize << 1 | this.rawChannels);
        System.arraycopy(this.data.raw(), 0, raw, 1, this.data.size());
        return raw;
    }

    public static FLVAudioPayload from(byte[] raw) {
        byte firstByte = raw[0];

        byte format = (byte) ((firstByte >> 4) & 0b1111);
        byte rate = (byte) ((firstByte >> 2) & 0b11);
        byte sampleSize = (byte) ((firstByte >> 1) & 0b1);
        byte channels = (byte) (firstByte & 0b1);

        byte[] dataBytes = new byte[raw.length - 1];
        System.arraycopy(raw, 1, dataBytes, 0, dataBytes.length);

        AudioData data = switch (format) {
            case 10 -> AACAudioData.from(dataBytes);
            default -> new UnknownAudioData(dataBytes);
        };

        return new FLVAudioPayload(
            format,
            rate,
            sampleSize,
            channels,
            data
        );
    }

    @Override
    public final String toString() {
        return String.format(
            "FLVAudioPayload[format=%s (%d), rate=%s (%d), sampleSize=%s (%d), channels=%s (%d), data=[len=%d], isSequenceHeader=%b]",
            this.format(),
            this.rawFormat,
            this.rate(),
            this.rawRate,
            this.sampleSize(),
            this.rawSampleSize,
            this.channels(),
            this.rawChannels,
            this.data,
            this.isSequenceHeader()
        );
    }

}
