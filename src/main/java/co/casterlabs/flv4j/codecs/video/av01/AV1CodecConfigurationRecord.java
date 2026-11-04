package co.casterlabs.flv4j.codecs.video.av01;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.flv4j.actionscript.io.ASByteView;
import co.casterlabs.flv4j.codecs.video.ChromaSubsampling;

// https://aomediacodec.github.io/av1-isobmff/#av1codecconfigurationbox-syntax
public record AV1CodecConfigurationRecord(ASByteView view) implements AV1PacketData {

    public int marker() {
        return this.view.u8(0) >> 7;
    }

    public int version() {
        return this.view.u8(0) & 0b1111111;
    }

    public int profile() {
        return this.view.u8(1) >> 5 & 0b111;
    }

    public int seqLevelIdx() {
        return this.view.u8(1) & 0b11111;
    }

    public int tier() {
        return this.view.u8(2) >> 7 & 1;
    }

    private boolean highBitdepth() {
        return (this.view.u8(2) >> 6 & 1) == 1;
    }

    private boolean twelveBit() {
        return (this.view.u8(2) >> 5 & 1) == 1;
    }

    public boolean monochrome() {
        return (this.view.u8(2) >> 4 & 1) == 1;
    }

    public int chromaSubsamplingX() {
        return this.view.u8(2) >> 3 & 1;
    }

    public int chromaSubsamplingY() {
        return this.view.u8(2) >> 2 & 1;
    }

    public int chromaSamplePosition() {
        return this.view.u8(2) & 3;
    }

    private int byte3() {
        if (this.view.length() < 4) {
            return 0;
        }
        return this.view.u8(3);
    }

    public int reserved3Bits() {
        return (this.byte3() >> 5) & 0b111;
    }

    public boolean initialPresentationDelayPresent() {
        return ((this.byte3() >> 4) & 1) == 1;
    }

    /**
     * @return initial_presentation_delay_minus_one when present, otherwise -1.
     */
    public int initialPresentationDelayMinusOne() {
        if (!this.initialPresentationDelayPresent()) {
            return -1;
        }
        return this.byte3() & 0b1111;
    }

    /**
     * @return low 4 reserved bits when initial_presentation_delay_present == 0,
     *         otherwise -1.
     */
    public int reserved4Bits() {
        if (this.initialPresentationDelayPresent()) {
            return -1;
        }
        return this.byte3() & 0b1111;
    }

    public @Nullable AV1OBUs configObus() {
        if (this.view.length() <= 4) {
            return null;
        }
        return new AV1OBUs(this.view.slice(4));
    }

    public AV1OBU[] sequenceHeaderObus() {
        return this._obusByType(AV1OBUType.SEQUENCE_HEADER);
    }

    public AV1SequenceHeader[] sequenceHeaders() {
        AV1OBU[] obus = this.sequenceHeaderObus();
        AV1SequenceHeader[] headers = new AV1SequenceHeader[obus.length];
        for (int i = 0; i < obus.length; i++) {
            headers[i] = AV1SequenceHeader.from(obus[i]);
        }
        return headers;
    }

    public AV1OBU[] metadata() {
        return this._obusByType(AV1OBUType.METADATA);
    }

    private AV1OBU[] _obusByType(AV1OBUType type) {
        AV1OBUs configObus = this.configObus();
        if (configObus == null) {
            return new AV1OBU[0];
        }

        List<AV1OBU> filtered = new ArrayList<>();
        for (AV1OBU obu : configObus.obus()) {
            if (obu.type() == type) {
                filtered.add(obu);
            }
        }
        return filtered.toArray(new AV1OBU[0]);
    }

    public int bitDepth() {
        if (this.twelveBit()) {
            return 12;
        }
        return this.highBitdepth() ? 10 : 8;
    }

    public ChromaSubsampling chromaSubsampling() {
        if (this.monochrome()) {
            return ChromaSubsampling.CS_400;
        }

        if (this.chromaSubsamplingX() == 0 && this.chromaSubsamplingY() == 0) return ChromaSubsampling.CS_444;
        if (this.chromaSubsamplingX() == 1 && this.chromaSubsamplingY() == 0) return ChromaSubsampling.CS_422;
        if (this.chromaSubsamplingX() == 1 && this.chromaSubsamplingY() == 1) return ChromaSubsampling.CS_420;

        throw new IllegalArgumentException(
            "Invalid chroma subsampling: x=" + this.chromaSubsamplingX() + ", y=" + this.chromaSubsamplingY()
        );
    }

    @Override
    public String toString() {
        return String.format(
            "AV1CodecConfigurationRecord{marker=%d, version=%d, profile=%d, seqLevelIdx=%d, tier=%d, bitDepth=%d (%s), monochrome=%b, chromaSubsampling=%s, chromaSamplePosition=%d, reserved3Bits=%d, initialPresentationDelayPresent=%b, initialPresentationDelayMinusOne=%d, reserved4Bits=%d, configObusLength=%d}",
            this.marker(),
            this.version(),
            this.profile(),
            this.seqLevelIdx(),
            this.tier(),
            this.bitDepth(), this.bitDepth() == 8 ? "8-bit" : this.bitDepth() == 10 ? "10-bit" : "12-bit",
            this.monochrome(),
            this.chromaSubsampling(),
            this.chromaSamplePosition(),
            this.reserved3Bits(),
            this.initialPresentationDelayPresent(),
            this.initialPresentationDelayMinusOne(),
            this.reserved4Bits(),
            this.configObus()
        );
    }

}
