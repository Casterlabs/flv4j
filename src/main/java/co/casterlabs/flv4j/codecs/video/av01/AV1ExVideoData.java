package co.casterlabs.flv4j.codecs.video.av01;

import co.casterlabs.flv4j.actionscript.io.ASByteView;
import co.casterlabs.flv4j.flv.tags.video.ex.FLVExVideoCodecData;
import co.casterlabs.flv4j.flv.tags.video.ex.FLVExVideoPacketType;

// https://veovera.org/docs/enhanced/enhanced-rtmp-v2#enhanced-video
public record AV1ExVideoData(
    int rawType,
    ASByteView view,
    AV1PacketData packetData
) implements FLVExVideoCodecData {

    public AV1ExVideoData(int rawType, ASByteView view) {
        this(
            rawType,
            view,
            switch (rawType) {
                case /*SEQUENCE_START*/ 0 -> new AV1CodecConfigurationRecord(view);
                case /*CODED_FRAMES*/ 1, /*CODED_FRAMES_X*/ 3 -> new AV1OBUs(view);
                // TODO MPEG2TSSequenceStart
                default -> new AV1PacketData.Invalid(view);
            }
        );
    }

    public static AV1ExVideoData from(FLVExVideoPacketType type, AV1PacketData packetData) {
        return from(type.id, packetData);
    }

    public static AV1ExVideoData from(int rawType, AV1PacketData packetData) {
        return new AV1ExVideoData(rawType, packetData.view(), packetData);
    }

    @Override
    public boolean isSequenceHeader() {
        return this.rawType == FLVExVideoPacketType.SEQUENCE_START.id;
    }

    @Override
    public int compositionTimeOffset() {
        return 0; // No CTS
    }

    @Override
    public String toString() {
        return String.format(
            "AV1ExVideoData[isSequenceHeader=%b, compositionTimeOffset=%d, data=%s]",
            this.isSequenceHeader(),
            this.compositionTimeOffset(),
            this.packetData
        );
    }

}
