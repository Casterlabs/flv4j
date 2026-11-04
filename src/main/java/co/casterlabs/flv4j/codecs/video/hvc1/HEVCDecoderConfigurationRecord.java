package co.casterlabs.flv4j.codecs.video.hvc1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public HEVCNaluType[] naluArrayTypes() {
        HEVCNaluType[] types = new HEVCNaluType[this.numOfArrays()];
        int offset = 23; // Fixed header size
        for (int i = 0; i < types.length; i++) {
            types[i] = HEVCNaluType.LUT[this.view.u8(offset) & 0b111111];
            int numNalus = this.view.u16(offset + 1);
            offset += 3;
            for (int j = 0; j < numNalus; j++) {
                int naluLength = this.view.u16(offset);
                offset += 2 + naluLength;
            }
        }
        return types;
    }

    public HEVCNalu[][] naluArrays() {
        HEVCNalu[][] arrays = new HEVCNalu[this.numOfArrays()][];
        int offset = 23; // Fixed header size
        for (int i = 0; i < arrays.length; i++) {
            offset++; // [array_completeness:1|nal_unit_type:7]
            int numNalus = this.view.u16(offset);
            offset += 2;

            HEVCNalu[] nalus = arrays[i] = new HEVCNalu[numNalus];
            for (int j = 0; j < nalus.length; j++) {
                int naluLength = this.view.u16(offset);
                offset += 2;
                nalus[j] = new HEVCNalu(this.view.slice(offset, naluLength));
                offset += naluLength;
            }
        }
        return arrays;
    }

    public HEVCNalu[] nalus(HEVCNaluType type) {
        List<HEVCNalu> nalus = new ArrayList<>();
        int offset = 23; // Fixed header size
        for (int i = 0; i < this.numOfArrays(); i++) {
            HEVCNaluType arrayType = HEVCNaluType.LUT[this.view.u8(offset) & 0b111111];
            int numNalus = this.view.u16(offset + 1);
            offset += 3;
            for (int j = 0; j < numNalus; j++) {
                int naluLength = this.view.u16(offset);
                offset += 2;
                if (arrayType == type) {
                    nalus.add(new HEVCNalu(this.view.slice(offset, naluLength)));
                }
                offset += naluLength;
            }
        }
        return nalus.toArray(new HEVCNalu[0]);
    }

    @Override
    public String toString() {
        return String.format(
            "HEVCDecoderConfigurationRecord{configurationVersion=%d, generalProfileSpace=%d, generalTierFlag=%b, generalProfileIdc=%d, generalProfileCompatibilityFlags=%d, generalConstraintIndicatorFlags=%d, generalLevelIdc=%d, minSpatialSegmentationIdc=%d, parallelismType=%d, chromaFormat=%d, bitDepthLumaMinus8=%d, bitDepthChromaMinus8=%d, avgFrameRate=%d, constantFrameRate=%d, numTemporalLayers=%d, temporalIdNested=%b, lengthSizeMinusOne=%d, numOfArrays=%d, naluArrays=%s}",
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
            this.numOfArrays(),
            Arrays.deepToString(this.naluArrays())
        );
    }

}
