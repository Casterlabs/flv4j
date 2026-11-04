package co.casterlabs.flv4j.codecs.video.hvc1;

import co.casterlabs.flv4j.actionscript.io.ASAssert;
import co.casterlabs.flv4j.actionscript.io.ASByteView;
import co.casterlabs.flv4j.codecs.VideoCodecData;
import co.casterlabs.flv4j.documentation.CreatesCopy;
import co.casterlabs.flv4j.flv.tags.video.ex.FLVExVideoPacketType;

// The non-standard version of H.265 used in FLV containers.
// It's similar to the AVC format https://veovera.org/docs/legacy/video-file-format-v10-0-spec.pdf#page=14
public record HEVCVideoData(
    ASByteView view,
    HEVCPacketData packetData
) implements VideoCodecData {

    public HEVCVideoData(ASByteView view) {
        this(
            view,
            switch (HEVCPacketType.LUT[view.u8(0)]) {
                case SEQUENCE_HEADER -> new HEVCDecoderConfigurationRecord(view.slice(4));
                case NALU -> new HEVCNalus(view.slice(4));
                default -> new HEVCPacketData.Invalid(view.slice(4));
            }
        );
    }

    @CreatesCopy
    public static HEVCVideoData from(HEVCPacketType type, int compositionTimeOffset, HEVCPacketData packetData) {
        return from(type.id, compositionTimeOffset, packetData);
    }

    @CreatesCopy
    public static HEVCVideoData from(int rawType, int compositionTimeOffset, HEVCPacketData packetData) {
        ASAssert.u8(rawType, "rawType");
        ASAssert.s24(compositionTimeOffset, "compositionTimeOffset");

        byte[] data = new byte[4 + packetData.view().length()];

        data[0] = (byte) (rawType & 0xFF);
        data[1] = (byte) ((compositionTimeOffset >> 16) & 0xFF);
        data[2] = (byte) ((compositionTimeOffset >> 8) & 0xFF);
        data[3] = (byte) ((compositionTimeOffset >> 0) & 0xFF);

        System.arraycopy(packetData.view().buffer(), packetData.view().offset(), data, 4, packetData.view().length());

        return new HEVCVideoData(new ASByteView(data));
    }

    public int rawType() {
        return this.view.u8(0);
    }

    public HEVCPacketType type() {
        return HEVCPacketType.LUT[this.rawType()];
    }

    @Override
    public boolean isSequenceHeader() {
        return this.rawType() == HEVCPacketType.SEQUENCE_HEADER.id;
    }

    @Override
    public int compositionTimeOffset() {
        return this.view.s24(1);
    }

    @Override
    public String toString() {
        return String.format(
            "HEVCVideoData[type=%s (%d), compositionTimeOffset=%d, data=%s]",
            this.type(), this.rawType(),
            this.compositionTimeOffset(),
            this.packetData
        );
    }

    // NB: We touch the Ex constructor directly to avoid a potential copy operation
    // in from() since the data layout is compatible when converting from std->ex.
    public HEVCExVideoData toEx() {
        if (this.rawType() == HEVCPacketType.SEQUENCE_HEADER.id) {
            return new HEVCExVideoData(FLVExVideoPacketType.SEQUENCE_START.id, this.packetData.view(), this.packetData);
        }

        if (this.rawType() == HEVCPacketType.END_OF_SEQUENCE.id) {
            return new HEVCExVideoData(FLVExVideoPacketType.SEQUENCE_END.id, this.packetData.view(), this.packetData);
        }

        // Invalid types get mangled into CODED_FRAMES... Oh well.

        if (this.compositionTimeOffset() == 0) {
            // Our CTS is 0, so we'll be nice and return CODED_FRAMES_X.
            return new HEVCExVideoData(FLVExVideoPacketType.CODED_FRAMES_X.id, this.packetData.view(), this.packetData);
        } else {
            // Coincidentally, we can slice off our type byte and return it as-is.
            return new HEVCExVideoData(FLVExVideoPacketType.CODED_FRAMES.id, this.view.slice(1), this.packetData);
        }
    }

}
