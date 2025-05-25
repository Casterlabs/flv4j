package co.casterlabs.flv4j.flv.tags.audio;

import java.io.IOException;

import co.casterlabs.flv4j.actionscript.io.ASAssert;
import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASSizer;
import co.casterlabs.flv4j.actionscript.io.ASWriter;
import co.casterlabs.flv4j.flv.tags.FLVTagData;
import co.casterlabs.flv4j.flv.tags.audio.data.AudioData;
import co.casterlabs.flv4j.flv.tags.audio.data.UnknownAudioData;
import co.casterlabs.flv4j.flv.tags.audio.data.aac.AACAudioData;

// https://rtmp.veriskope.com/pdf/video_file_format_spec_v10.pdf#page=10
// https://veovera.org/docs/enhanced/enhanced-rtmp-v2#enhanced-audio
public record FLVAudioTagData(
    int rawFormat,
    int rawRate,
    int rawSampleSize,
    int rawChannels,
    AudioData data
) implements FLVTagData {

    public FLVAudioTagData(int rawFormat, int rawRate, int rawSampleSize, int rawChannels, AudioData data) {
        ASAssert.u4(rawFormat, "rawFormat");
        ASAssert.u2(rawRate, "rawRate");
        ASAssert.bit(rawSampleSize, "rawSampleSize");
        ASAssert.bit(rawChannels, "rawChannels");
        assert data != null : "data cannot be null";
        this.rawFormat = rawFormat;
        this.rawRate = rawRate;
        this.rawSampleSize = rawSampleSize;
        this.rawChannels = rawChannels;
        this.data = data;
    }

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
        return new ASSizer()
            .u8()
            .bytes(this.data.size()).size;
    }

    @Override
    public void serialize(ASWriter writer) throws IOException {
        int fb = this.rawFormat << 4 | this.rawRate << 2 | this.rawSampleSize << 1 | this.rawChannels;
        writer.u8(fb);
        this.data.serialize(writer);
    }

    public static FLVAudioTagData parse(ASReader reader, int length) throws IOException {
        int fb = reader.u8();

        int format = fb >> 4 & 0b1111;
        int rate = fb >> 2 & 0b11;
        int sampleSize = fb >> 1 & 0b1;
        int channels = fb & 0b1;

        int dataLen = length - 1;
        AudioData data = switch (format) {
            case 10 -> AACAudioData.parse(reader.limited(dataLen), dataLen);
            default -> new UnknownAudioData(reader.bytes(dataLen));
        };

        return new FLVAudioTagData(
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
            "FLVAudioPayload[format=%s (%d), rate=%s (%d), sampleSize=%s (%d), channels=%s (%d), data=%s, isSequenceHeader=%b]",
            this.format(), this.rawFormat,
            this.rate(), this.rawRate,
            this.sampleSize(), this.rawSampleSize,
            this.channels(), this.rawChannels,
            this.data,
            this.isSequenceHeader()
        );
    }

}
