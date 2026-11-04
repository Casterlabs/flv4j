package co.casterlabs.flv4j.codecs.audio.aac;

import co.casterlabs.flv4j.actionscript.io.ASByteView;
import co.casterlabs.flv4j.codecs.AudioCodecData;
import co.casterlabs.flv4j.documentation.CreatesCopy;
import co.casterlabs.flv4j.flv.tags.audio.ex.FLVExAudioPacketType;

// https://veovera.org/docs/legacy/video-file-format-v10-0-spec.pdf#page=12
public record AACAudioData(
    ASByteView view,
    AACPacketData packetData
) implements AudioCodecData {

    public AACAudioData(ASByteView view) {
        this(
            view,
            switch (view.u8(0)) {
                case /* SEQUENCE_HEADER */ 0 -> new AACAudioSpecificConfig(view.slice(1));
                case /* RAW */ 1 -> new AACCodedData(view.slice(1));
                default -> new AACPacketData.Invalid(view.slice(1));
            }
        );
    }

    @CreatesCopy
    public static AACAudioData from(AACPacketType type, AACPacketData packetData) {
        return from(type.id, packetData);
    }

    @CreatesCopy
    public static AACAudioData from(int rawType, AACPacketData packetData) {
        byte[] buf = new byte[packetData.view().length() + 1];
        buf[0] = (byte) rawType;
        System.arraycopy(packetData.view().buffer(), packetData.view().offset(), buf, 1, packetData.view().length());

        return new AACAudioData(new ASByteView(buf));
    }

    public int rawType() {
        return this.view.u8(0);
    }

    public AACPacketType type() {
        return AACPacketType.LUT[this.rawType()];
    }

    @Override
    public boolean isSequenceHeader() {
        return this.rawType() == AACPacketType.SEQUENCE_HEADER.id;
    }

    @Override
    public String toString() {
        return String.format(
            "AACAudioData[type=%s (%d), isSequenceHeader=%b, data=%s]",
            this.type(), this.rawType(),
            this.isSequenceHeader(),
            this.packetData
        );
    }

    @CreatesCopy
    public AACExAudioData toEx() {
        return AACExAudioData.from(
            switch (this.rawType()) {
                case /* SEQUENCE_HEADER */ 0 -> FLVExAudioPacketType.SEQUENCE_START;
                case /* RAW */ 1 -> FLVExAudioPacketType.CODED_FRAMES;
                default -> FLVExAudioPacketType.CODED_FRAMES; // Mangle... Oh well.
            },
            this.packetData
        );
    }

}
