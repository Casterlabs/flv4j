package co.casterlabs.flv4j.codecs.video.hvc1;

import co.casterlabs.flv4j.actionscript.io.ASAssert;
import co.casterlabs.flv4j.actionscript.io.ASByteView;
import co.casterlabs.flv4j.codecs.video.avc1.AVCPacketData;
import co.casterlabs.flv4j.flv.tags.video.ex.FLVExVideoCodecData;
import co.casterlabs.flv4j.flv.tags.video.ex.FLVExVideoPacketType;

// https://veovera.org/docs/enhanced/enhanced-rtmp-v2#enhanced-video
public record HEVCExVideoData(
    int rawType,
    ASByteView view,
    HEVCPacketData packetData
) implements FLVExVideoCodecData {

    public HEVCExVideoData(int rawType, ASByteView view) {
        this(
            rawType,
            view,
            switch (rawType) {
                case /*SEQUENCE_START*/ 0 -> new HEVCDecoderConfigurationRecord(view);
                case /*CODED_FRAMES*/ 1 -> new HEVCPacketData.Invalid(view.slice(3));
                default -> new HEVCPacketData.Invalid(view);
            }
        );
    }

    /**
     * @param compositionTimeOffset unused unless the type is CODED_FRAMES.
     */
    public static HEVCExVideoData from(FLVExVideoPacketType type, int compositionTimeOffset, AVCPacketData packetData) {
        return from(type.id, compositionTimeOffset, packetData);
    }

    /**
     * @param compositionTimeOffset unused unless the type is CODED_FRAMES.
     */
    public static HEVCExVideoData from(int rawType, int compositionTimeOffset, AVCPacketData packetData) {
        byte[] data;
        if (rawType == FLVExVideoPacketType.CODED_FRAMES.id) {
            ASAssert.s24(compositionTimeOffset, "compositionTimeOffset");

            data = new byte[3 + packetData.view().length()];

            data[0] = (byte) ((compositionTimeOffset >> 16) & 0xFF);
            data[1] = (byte) ((compositionTimeOffset >> 8) & 0xFF);
            data[2] = (byte) ((compositionTimeOffset >> 0) & 0xFF);

            System.arraycopy(packetData.view().buffer(), packetData.view().offset(), data, 3, packetData.view().length());
        } else {
            data = packetData.view().raw();
        }

        return new HEVCExVideoData(rawType, new ASByteView(data));
    }

    @Override
    public boolean isSequenceHeader() {
        return this.rawType == FLVExVideoPacketType.SEQUENCE_START.id;
    }

    @Override
    public int compositionTimeOffset() {
        if (this.rawType == FLVExVideoPacketType.CODED_FRAMES.id) {
            return this.view.s24(0);
        }
        return 0; // The others don't have a CTS.
    }

    @Override
    public String toString() {
        return String.format(
            "HEVCExVideoData[isSequenceHeader=%b, compositionTimeOffset=%d, data=%s]",
            this.isSequenceHeader(),
            this.compositionTimeOffset(),
            this.packetData
        );
    }

}
