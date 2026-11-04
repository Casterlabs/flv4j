package co.casterlabs.flv4j.codecs.audio.flac;

import co.casterlabs.flv4j.actionscript.io.ASByteView;
import co.casterlabs.flv4j.flv.tags.audio.ex.FLVExAudioCodecData;
import co.casterlabs.flv4j.flv.tags.audio.ex.FLVExAudioPacketType;

// https://veovera.org/docs/enhanced/enhanced-rtmp-v2#enhanced-audio
public record FLACExAudioData(
    int rawType,
    ASByteView view,
    FLACPacketData packetData
) implements FLVExAudioCodecData {

    public FLACExAudioData(int rawType, ASByteView view) {
        this(
            rawType,
            view,
            switch (rawType) {
                case /*SEQUENCE_START*/ 0 -> new FLACStreamInfo(view);
                case /*CODED_FRAMES*/ 1 -> new FLACCodedData(view);
                default -> new FLACPacketData.Invalid(view);
            }
        );
    }

    public static FLACExAudioData from(FLVExAudioPacketType type, FLACPacketData packetData) {
        return from(type.id, packetData);
    }

    public static FLACExAudioData from(int rawType, FLACPacketData packetData) {
        return new FLACExAudioData(rawType, packetData.view(), packetData);
    }

    @Override
    public boolean isSequenceHeader() {
        return this.rawType == FLVExAudioPacketType.SEQUENCE_START.id;
    }

    @Override
    public String toString() {
        return String.format(
            "FlacExAudioData[isSequenceHeader=%b, data=%s]",
            this.isSequenceHeader(),
            this.packetData
        );
    }

}
