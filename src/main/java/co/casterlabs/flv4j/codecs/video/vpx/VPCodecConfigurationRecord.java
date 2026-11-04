package co.casterlabs.flv4j.codecs.video.vpx;

import co.casterlabs.flv4j.actionscript.io.ASByteView;
import co.casterlabs.flv4j.codecs.video.ChromaSubsampling;
import co.casterlabs.flv4j.codecs.video.ChromaSubsamplingPosition;
import co.casterlabs.flv4j.codecs.video.ColorPrimaries;
import co.casterlabs.flv4j.codecs.video.MatrixCoefficients;
import co.casterlabs.flv4j.codecs.video.TransferCharacteristics;

// https://www.webmproject.org/vp9/mp4/
public record VPCodecConfigurationRecord(ASByteView view) implements VPPacketData {

    public int configurationVersion() {
        return this.view.u8(0);
    }

    public int reserved() {
        return this.view.u24(1);
    }

    public int profile() {
        return this.view.u8(4);
    }

    public int level() {
        return this.view.u8(5);
    }

    public int bitDepth() {
        // NB: actual bit depth (8/10/12), not minus-8.
        return this.view.u8(6) >> 4;
    }

    public ChromaSubsampling chromaSubsampling() {
        return switch ((this.view.u8(6) >> 1) & 0x03) {
            case 0, 1 -> ChromaSubsampling.CS_420;
            case 2 -> ChromaSubsampling.CS_422;
            case 3 -> ChromaSubsampling.CS_444;
            default -> ChromaSubsampling.UNKNOWN;
        };
    }

    public ChromaSubsamplingPosition chromaSamplePosition() {
        return switch ((this.view.u8(6) >> 1) & 0x03) {
            case 0 -> ChromaSubsamplingPosition.VERTICAL;
            case 1 -> ChromaSubsamplingPosition.COLOCATED;
            default -> ChromaSubsamplingPosition.NOT_APPLICABLE;
        };
    }

    public boolean videoFullRangeFlag() {
        return (this.view.u8(6) & 0x01) != 0;
    }

    public ColorPrimaries colourPrimaries() {
        return ColorPrimaries.fromH273(this.view.u8(7));
    }

    public TransferCharacteristics transferCharacteristics() {
        return TransferCharacteristics.fromH273(this.view.u8(8));
    }

    public MatrixCoefficients matrixCoefficients() {
        return MatrixCoefficients.fromH273(this.view.u8(9));
    }

    public int codecInitialisationDataSize() {
        return this.view.u16(10);
    }

    @Override
    public String toString() {
        return String.format(
            "VPCodecConfigurationRecord{configurationVersion=%d, profile=%d, level=%d, bitDepth=%d, chromaSubsampling=%s, chromaSamplePosition=%s, videoFullRangeFlag=%b, colourPrimaries=%s, transferCharacteristics=%s, matrixCoefficients=%s, codecInitialisationDataSize=%d}",
            this.configurationVersion(),
            this.profile(),
            this.level(),
            this.bitDepth(),
            this.chromaSubsampling(),
            this.chromaSamplePosition(),
            this.videoFullRangeFlag(),
            this.colourPrimaries(),
            this.transferCharacteristics(),
            this.matrixCoefficients(),
            this.codecInitialisationDataSize()
        );
    }

}
