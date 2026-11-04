package co.casterlabs.flv4j.codecs.audio.aac;

import co.casterlabs.flv4j.actionscript.io.ASByteView;
import co.casterlabs.flv4j.documentation.CreatesCopy;
import co.casterlabs.flv4j.flv.tags.audio.ex.FLVExAudioCodecData;
import co.casterlabs.flv4j.flv.tags.audio.ex.FLVExAudioPacketType;

// https://veovera.org/docs/enhanced/enhanced-rtmp-v2#enhanced-audio
public record AACExAudioData(
    int rawType,
    ASByteView view,
    AACPacketData packetData
) implements FLVExAudioCodecData {

    public AACExAudioData(int rawType, ASByteView view) {
        this(
            rawType,
            view,
            switch (rawType) {
                case /*SEQUENCE_START*/ 0 -> new AACAudioSpecificConfig(view);
                case /*CODED_FRAMES*/ 1 -> new AACCodedData(view);
                default -> new AACPacketData.Invalid(view);
            }
        );
    }

    public static AACExAudioData from(FLVExAudioPacketType type, AACPacketData packetData) {
        return from(type.id, packetData);
    }

    public static AACExAudioData from(int rawType, AACPacketData packetData) {
        return new AACExAudioData(rawType, packetData.view(), packetData);
    }

    public FLVExAudioPacketType type() {
        return FLVExAudioPacketType.LUT[this.rawType];
    }

    @Override
    public boolean isSequenceHeader() {
        return this.rawType == FLVExAudioPacketType.SEQUENCE_START.id;
    }

    @Override
    public String toString() {
        return String.format(
            "AACExAudioData[isSequenceHeader=%b, data=%s]",
            this.isSequenceHeader(),
            this.packetData
        );
    }

    @CreatesCopy
    public AACAudioData toStd() {
        return AACAudioData.from(
            switch (this.rawType) {
                case /*SEQUENCE_START*/ 0 -> AACPacketType.SEQUENCE_HEADER;
                case /*CODED_FRAMES*/ 1 -> AACPacketType.RAW;
                default -> AACPacketType.RAW; // Mangle... Oh well.
            },
            this.packetData
        );
    }

}
