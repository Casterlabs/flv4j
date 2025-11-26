package co.casterlabs.flv4j.codecs.video.hvc1;

import co.casterlabs.flv4j.actionscript.io.ASAssert;
import co.casterlabs.flv4j.actionscript.io.ASByteView;
import co.casterlabs.flv4j.codecs.VideoCodecData;

// The non-standard version of H.265 used in FLV containers.
// It's similar to the AVC format.
public record HEVCVideoData(
    ASByteView view,
    HEVCPacketData packetData
) implements VideoCodecData {

    public HEVCVideoData(ASByteView view) {
        this(
            view,
            switch (HEVCPacketType.LUT[view.u8(0)]) {
                case SEQUENCE_HEADER -> new HEVCDecoderConfigurationRecord(view.slice(4));
                default -> new HEVCPacketData.Invalid(view.slice(4));
            }
        );
    }

    public static HEVCVideoData from(HEVCPacketType type, int compositionTimeOffset, HEVCPacketData packetData) {
        return from(type.id, compositionTimeOffset, packetData);
    }

    public static HEVCVideoData from(int rawType, int compositionTimeOffset, HEVCPacketData packetData) {
        ASAssert.u8(rawType, "rawType");
        ASAssert.u24(compositionTimeOffset, "compositionTimeOffset");

        byte[] data = new byte[4 + packetData.view().length()];

        data[0] = (byte) (rawType & 0xFF);
        data[1] = (byte) ((compositionTimeOffset >> 16) & 0xFF);
        data[2] = (byte) ((compositionTimeOffset >> 8) & 0xFF);
        data[3] = (byte) ((compositionTimeOffset >> 0) & 0xFF);

        System.arraycopy(packetData.view().buffer(), packetData.view().offset(), data, 4, packetData.view().length());

        return new HEVCVideoData(new ASByteView(data));
    }

    public HEVCPacketType type() {
        int rawType = this.view.u8(0);
        return HEVCPacketType.LUT[rawType];
    }

    @Override
    public boolean isSequenceHeader() {
        int rawType = this.view.u8(0);
        return rawType == HEVCPacketType.SEQUENCE_HEADER.id;
    }

    @Override
    public int compositionTimeOffset() {
        return this.view.u24(1);
    }

    @Override
    public String toString() {
        return String.format(
            "HEVCVideoData[type=%s (%d), compositionTimeOffset=%d, data=%s]",
            this.type(), this.view.u8(0),
            this.compositionTimeOffset(),
            this.packetData
        );
    }

}
