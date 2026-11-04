package co.casterlabs.flv4j.codecs.audio.opus;

import co.casterlabs.flv4j.actionscript.io.ASByteView;
import co.casterlabs.flv4j.flv.tags.audio.ex.FLVExAudioCodecData;
import co.casterlabs.flv4j.flv.tags.audio.ex.FLVExAudioPacketType;

// https://veovera.org/docs/enhanced/enhanced-rtmp-v2#enhanced-audio
public record OpusExAudioData(
    int rawType,
    ASByteView view,
    OpusPacketData packetData
) implements FLVExAudioCodecData {

    public OpusExAudioData(int rawType, ASByteView view) {
        this(
            rawType,
            view,
            switch (rawType) {
                case /*SEQUENCE_START*/ 0 -> new OpusHead(view);
                case /*CODED_FRAMES*/ 1 -> new OpusCodedData(view);
                default -> new OpusPacketData.Invalid(view);
            }
        );
    }

    public static OpusExAudioData from(FLVExAudioPacketType type, OpusPacketData packetData) {
        return from(type.id, packetData);
    }

    public static OpusExAudioData from(int rawType, OpusPacketData packetData) {
        return new OpusExAudioData(rawType, packetData.view(), packetData);
    }

    @Override
    public boolean isSequenceHeader() {
        return this.rawType == FLVExAudioPacketType.SEQUENCE_START.id;
    }

    @Override
    public String toString() {
        return String.format(
            "OpusExAudioData[isSequenceHeader=%b, data=%s]",
            this.isSequenceHeader(),
            this.packetData
        );
    }

}
