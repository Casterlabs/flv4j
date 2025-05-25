package co.casterlabs.flv4j.flv.tags.audio;

import java.io.IOException;

import co.casterlabs.flv4j.actionscript.io.ASAssert;
import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASSizer;
import co.casterlabs.flv4j.actionscript.io.ASWriter;
import co.casterlabs.flv4j.flv.tags.audio.data.AudioData;

// https://rtmp.veriskope.com/pdf/video_file_format_spec_v10.pdf#page=10
// https://veovera.org/docs/enhanced/enhanced-rtmp-v2#enhanced-audio
public record FLVStandardAudioTagData(
    int rawFormat,
    int rawRate,
    int rawSampleSize,
    int rawChannels,
    AudioData data
) implements FLVAudioTagData {

    public FLVStandardAudioTagData(int rawFormat, int rawRate, int rawSampleSize, int rawChannels, AudioData data) {
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

    @Override
    public boolean isEx() {
        return false;
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
        return switch (this.format()) {
//            case AAC -> ((AACAudioData) this.data).rawType() == 0;
            case AAC -> this.data.raw()[0] == 0;
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

    public static FLVStandardAudioTagData parse(int fb, ASReader reader, int length) throws IOException {
        int format = fb >> 4 & 0b1111;
        int rate = fb >> 2 & 0b11;
        int sampleSize = fb >> 1 & 0b1;
        int channels = fb & 0b1;

        int dataLen = length - 1;
        AudioData data = switch (format) {
            default -> new AudioData(reader.bytes(dataLen));
        };

        return new FLVStandardAudioTagData(
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
