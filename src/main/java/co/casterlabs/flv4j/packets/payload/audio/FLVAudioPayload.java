package co.casterlabs.flv4j.packets.payload.audio;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.flv4j.packets.payload.FLVPayload;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

// https://rtmp.veriskope.com/pdf/video_file_format_spec_v10.pdf#page=10
// https://veovera.org/docs/enhanced/enhanced-rtmp-v2#enhanced-audio
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class FLVAudioPayload implements FLVPayload {
    private @Nullable byte[] raw;

    private final int rawFormat;
    private final int rawRate;
    private final int rawSampleSize;
    private final int rawChannels;
    private final byte[] data;

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
            this.data.length,
            this.isSequenceHeader()
        );
    }

    @Override
    public boolean isSequenceHeader() {
        if (this.isExHeader()) {
            byte packetType = (byte) (this.rawRate << 2 | this.rawSampleSize << 1 | this.rawChannels); // Reconstruct the lower-4 bits of the first byte.
            return packetType == 0;
        } else {
            switch (this.format()) {
                case AAC:
                    return this.data[0] == 0;
                default:
                    return false; // TODO properly parse out the data.
            }
        }
    }

    @Override
    public byte[] raw() {
        if (this.raw == null) {
            // We don't have raw data cached. We need to create it.
            this.raw = new byte[this.size()];
            this.raw[0] = (byte) (this.rawFormat << 4 | this.rawRate << 2 | this.rawSampleSize << 1 | this.rawChannels);
            System.arraycopy(this.data, 0, this.raw, 1, this.data.length);
        }

        return this.raw;
    }

    @Override
    public int size() {
        return 1 + this.data.length;
    }

    @Override
    public FLVAudioPayload clone() {
        return new FLVAudioPayload(this.rawFormat, this.rawRate, this.rawSampleSize, this.rawChannels, this.data);
    }

    public static FLVAudioPayload from(byte[] raw) {
        byte firstByte = raw[0];

        byte format = (byte) ((firstByte >> 4) & 0b1111);
        byte rate = (byte) ((firstByte >> 2) & 0b11);
        byte sampleSize = (byte) ((firstByte >> 1) & 0b1);
        byte channels = (byte) (firstByte & 0b1);

        byte[] data = new byte[raw.length - 1];
        System.arraycopy(raw, 1, data, 0, data.length);

        FLVAudioPayload payload = new FLVAudioPayload(
            format,
            rate,
            sampleSize,
            channels,
            data
        );
        payload.raw = raw;
        return payload;
    }

}
