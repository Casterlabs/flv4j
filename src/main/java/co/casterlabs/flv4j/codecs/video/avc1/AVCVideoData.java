package co.casterlabs.flv4j.codecs.video.avc1;

import co.casterlabs.flv4j.actionscript.io.ASAssert;
import co.casterlabs.flv4j.actionscript.io.ASByteView;
import co.casterlabs.flv4j.codecs.VideoCodecData;
import co.casterlabs.flv4j.documentation.CreatesCopy;
import co.casterlabs.flv4j.flv.tags.video.ex.FLVExVideoPacketType;

// https://veovera.org/docs/legacy/video-file-format-v10-0-spec.pdf#page=14
public record AVCVideoData(
    ASByteView view,
    AVCPacketData packetData
) implements VideoCodecData {

    public AVCVideoData(ASByteView view) {
        this(
            view,
            switch (AVCPacketType.LUT[view.u8(0)]) {
                case SEQUENCE_HEADER -> new AVCDecoderConfigurationRecord(view.slice(4));
                case NALU -> new AVCNalus(view.slice(4));
                default -> new AVCPacketData.Invalid(view.slice(4));
            }
        );
    }

    @CreatesCopy
    public static AVCVideoData from(AVCPacketType type, int compositionTimeOffset, AVCPacketData packetData) {
        return from(type.id, compositionTimeOffset, packetData);
    }

    @CreatesCopy
    public static AVCVideoData from(int rawType, int compositionTimeOffset, AVCPacketData packetData) {
        ASAssert.u8(rawType, "rawType");
        ASAssert.s24(compositionTimeOffset, "compositionTimeOffset");

        byte[] data = new byte[4 + packetData.view().length()];

        data[0] = (byte) (rawType & 0xFF);
        data[1] = (byte) ((compositionTimeOffset >> 16) & 0xFF);
        data[2] = (byte) ((compositionTimeOffset >> 8) & 0xFF);
        data[3] = (byte) ((compositionTimeOffset >> 0) & 0xFF);

        System.arraycopy(packetData.view().buffer(), packetData.view().offset(), data, 4, packetData.view().length());

        return new AVCVideoData(new ASByteView(data));
    }

    public int rawType() {
        return this.view.u8(0);
    }

    public AVCPacketType type() {
        return AVCPacketType.LUT[this.rawType()];
    }

    @Override
    public boolean isSequenceHeader() {
        return this.rawType() == AVCPacketType.SEQUENCE_HEADER.id;
    }

    @Override
    public int compositionTimeOffset() {
        return this.view.s24(1);
    }

    @Override
    public String toString() {
        return String.format(
            "AVCVideoData[type=%s (%d), compositionTimeOffset=%d, data=%s]",
            this.type(), this.rawType(),
            this.compositionTimeOffset(),
            this.packetData
        );
    }

    // NB: We touch the Ex constructor directly rather than using the from() utility
    // to avoid a potential copy operation in from() since the data layout is
    // compatible when converting from std->ex.
    public AVCExVideoData toEx() {
        if (this.rawType() == AVCPacketType.SEQUENCE_HEADER.id) {
            return new AVCExVideoData(FLVExVideoPacketType.SEQUENCE_START.id, this.packetData.view(), this.packetData);
        }

        if (this.rawType() == AVCPacketType.END_OF_SEQUENCE.id) {
            return new AVCExVideoData(FLVExVideoPacketType.SEQUENCE_END.id, this.packetData.view(), this.packetData);
        }

        // Invalid types get mangled into NALU... Oh well.

        if (this.compositionTimeOffset() == 0) {
            // Our CTS is 0, so we'll be nice and return CODED_FRAMES_X.
            return new AVCExVideoData(FLVExVideoPacketType.CODED_FRAMES_X.id, this.packetData.view(), this.packetData);
        } else {
            // Coincidentally, we can slice off our type byte and return it as-is.
            return new AVCExVideoData(FLVExVideoPacketType.CODED_FRAMES.id, this.view.slice(1), this.packetData);
        }
    }

}
