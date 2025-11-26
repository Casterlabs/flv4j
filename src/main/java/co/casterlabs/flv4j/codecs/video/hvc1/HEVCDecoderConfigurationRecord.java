package co.casterlabs.flv4j.codecs.video.hvc1;

import co.casterlabs.flv4j.actionscript.io.ASByteView;

// https://ossrs.io/lts/zh-cn/assets/files/ISO_IEC_14496-15-AVC-format-2017-e51a22786929a5fbc2f9dc9fc4761e09.pdf#page=78
public record HEVCDecoderConfigurationRecord(ASByteView view) implements HEVCPacketData {

    public int configurationVersion() {
        return this.view.u8(0);
    }

    public int generalProfileSpace() {
        return (this.view.u8(1) >> 6) & 0x03;
    }

    public boolean generalTierFlag() {
        return ((this.view.u8(1) >> 5) & 0x01) != 0;
    }

    public int generalProfileIdc() {
        return this.view.u8(1) & 0x1F;
    }

    public long generalProfileCompatibilityFlags() {
        return this.view.u32(2);
    }

    public long generalConstraintIndicatorFlags() {
        return this.view.u48(6);
    }

    public int generalLevelIdc() {
        return this.view.u8(12);
    }

    public int minSpatialSegmentationIdc() {
        return this.view.u16(13) & 0x0FFF;
    }

    public int parallelismType() {
        return this.view.u8(15) & 0x03;
    }

    public int chromaFormat() {
        return this.view.u8(16) & 0x03;
    }

    public int bitDepthLumaMinus8() {
        return this.view.u8(17) & 0x07;
    }

    public int bitDepthChromaMinus8() {
        return this.view.u8(18) & 0x07;
    }

    public int avgFrameRate() {
        return this.view.u16(19);
    }

    public int constantFrameRate() {
        return (this.view.u8(21) >> 6) & 0x03;
    }

    public int numTemporalLayers() {
        return (this.view.u8(21) >> 3) & 0x07;
    }

    public boolean temporalIdNested() {
        return ((this.view.u8(21) >> 2) & 0x01) != 0;
    }

    public int lengthSizeMinusOne() {
        return this.view.u8(21) & 0x03;
    }

    public int numOfArrays() {
        return this.view.u8(22);
    }

    // TODO nalus

    @Override
    public String toString() {
        return String.format(
            "HEVCDecoderConfigurationRecord{configurationVersion=%d, generalProfileSpace=%d, generalTierFlag=%b, generalProfileIdc=%d, generalProfileCompatibilityFlags=%d, generalConstraintIndicatorFlags=%d, generalLevelIdc=%d, minSpatialSegmentationIdc=%d, parallelismType=%d, chromaFormat=%d, bitDepthLumaMinus8=%d, bitDepthChromaMinus8=%d, avgFrameRate=%d, constantFrameRate=%d, numTemporalLayers=%d, temporalIdNested=%b, lengthSizeMinusOne=%d, numOfArrays=%d}",
            this.configurationVersion(),
            this.generalProfileSpace(),
            this.generalTierFlag(),
            this.generalProfileIdc(),
            this.generalProfileCompatibilityFlags(),
            this.generalConstraintIndicatorFlags(),
            this.generalLevelIdc(),
            this.minSpatialSegmentationIdc(),
            this.parallelismType(),
            this.chromaFormat(),
            this.bitDepthLumaMinus8(),
            this.bitDepthChromaMinus8(),
            this.avgFrameRate(),
            this.constantFrameRate(),
            this.numTemporalLayers(),
            this.temporalIdNested(),
            this.lengthSizeMinusOne(),
            this.numOfArrays()
        );
    }

}
