package co.casterlabs.flv4j.codecs.video.vpx;

import co.casterlabs.flv4j.actionscript.io.ASByteView;
import co.casterlabs.flv4j.flv.tags.video.ex.FLVExVideoCodecData;
import co.casterlabs.flv4j.flv.tags.video.ex.FLVExVideoPacketType;

// https://veovera.org/docs/enhanced/enhanced-rtmp-v2#enhanced-video
public record VPExVideoData(
    int rawType,
    ASByteView view,
    VPPacketData packetData
) implements FLVExVideoCodecData {

    public VPExVideoData(int rawType, ASByteView view) {
        this(
            rawType,
            view,
            switch (rawType) {
                case /*SEQUENCE_START*/ 0 -> new VPCodecConfigurationRecord(view);
                case /*CODED_FRAMES*/ 1, /*CODED_FRAMES_X*/ 3 -> new VPCodedData(view);
                default -> new VPPacketData.Invalid(view);
            }
        );
    }

    public static VPExVideoData from(FLVExVideoPacketType type, VPPacketData packetData) {
        return from(type.id, packetData);
    }

    public static VPExVideoData from(int rawType, VPPacketData packetData) {
        return new VPExVideoData(rawType, packetData.view(), packetData);
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
            "VPExVideoData[isSequenceHeader=%b, compositionTimeOffset=%d, data=%s]",
            this.isSequenceHeader(),
            this.compositionTimeOffset(),
            this.packetData
        );
    }

}
