package co.casterlabs.flv4j.flv.tags.audio;

import co.casterlabs.flv4j.FLVBVRawSerializable;
import co.casterlabs.flv4j.actionscript.io.ASByteView;
import co.casterlabs.flv4j.codecs.AudioCodecData;
import co.casterlabs.flv4j.codecs.audio.aac.AACAudioData;

// https://veovera.org/docs/legacy/video-file-format-v10-0-spec.pdf#page=10
// https://veovera.org/docs/enhanced/enhanced-rtmp-v2#enhanced-audio
public record FLVStandardAudioTagData(
    ASByteView view,
    AudioCodecData data
) implements FLVAudioTagData, FLVBVRawSerializable {

    public FLVStandardAudioTagData(ASByteView view) {
        this(
            view,
            switch (view.u8(0) >> 4 & 0b1111) {
                case 10 -> new AACAudioData(view.slice(1));
                default -> new AudioCodecData.Invalid(view.slice(1));
            }
        );
    }

    public static FLVStandardAudioTagData from(FLVAudioFormat format, FLVAudioRate rate, FLVAudioSampleSize sampleSize, FLVAudioChannels channels, AudioCodecData data) {
        return from(format.id, rate.id, sampleSize.id, channels.id, data);
    }

    public static FLVStandardAudioTagData from(int rawFormat, int rawRate, int rawSampleSize, int rawChannels, AudioCodecData audioData) {
        ASByteView dataView = audioData.view();

        byte[] bytes = new byte[1 + dataView.length()];

        bytes[0] = (byte) (((rawFormat & 0b1111) << 4) |
            ((rawRate & 0b11) << 2) |
            ((rawSampleSize & 0b1) << 1) |
            (rawChannels & 0b1));

        System.arraycopy(dataView.buffer(), dataView.offset(), bytes, 1, dataView.length());

        return new FLVStandardAudioTagData(new ASByteView(bytes));
    }

    @Override
    public boolean isEx() {
        return false;
    }

    public FLVAudioFormat format() {
        int rawFormat = this.view.u8(0) >> 4 & 0b1111;
        return FLVAudioFormat.LUT[rawFormat];
    }

    public FLVAudioRate rate() {
        int rawRate = this.view.u8(0) >> 2 & 0b11;
        return FLVAudioRate.LUT[rawRate];
    }

    public FLVAudioSampleSize sampleSize() {
        int rawSampleSize = this.view.u8(0) >> 1 & 0b1;
        return FLVAudioSampleSize.LUT[rawSampleSize];
    }

    public FLVAudioChannels channels() {
        int rawChannels = this.view.u8(0) & 0b1;
        return FLVAudioChannels.LUT[rawChannels];
    }

    @Override
    public boolean isSequenceHeader() {
        return this.data.isSequenceHeader();
    }

    @Override
    public final String toString() {
        return String.format(
            "FLVAudioPayload[format=%s (%d), rate=%s (%d), sampleSize=%s (%d), channels=%s (%d), isSequenceHeader=%b, data=%s]",
            this.format(), this.view.u8(0) >> 4 & 0b1111,
            this.rate(), this.view.u8(0) >> 2 & 0b11,
            this.sampleSize(), this.view.u8(0) >> 1 & 0b1,
            this.channels(), this.view.u8(0) & 0b1,
            this.data,
            this.isSequenceHeader()
        );
    }

}
