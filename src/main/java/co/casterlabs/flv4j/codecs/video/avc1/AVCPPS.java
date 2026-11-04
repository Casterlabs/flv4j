package co.casterlabs.flv4j.codecs.video.avc1;

import co.casterlabs.flv4j.actionscript.io.ASBitReader;
import co.casterlabs.flv4j.documentation.CreatesCopy;

public record AVCPPS(
    int ppsId,
    int spsId,
    boolean entropicCodingMode,
    boolean bottomFieldPicOrderInFramePresent,
    int numRefIdxL0DefaultActiveMinus1,
    int numRefIdxL1DefaultActiveMinus1,
    boolean weightedPred,
    int weightedBipredIdc,
    int picInitQpMinus26,
    int picInitQsMinus26,
    int chromaQpIndexOffset,
    boolean deblockingFilterControlPresent,
    boolean constrainedIntraPred,
    boolean redundantPicCntPresent,
    boolean transform8x8Mode,
    int secondChromaQpIndexOffset
) {

    @CreatesCopy
    public static AVCPPS from(AVCNalu nalu) {
        if (nalu.type() != AVCNaluType.PPS) {
            throw new IllegalArgumentException("Expected a PPS NALU, got: " + nalu.type());
        }

        ASBitReader b = new ASBitReader(nalu.rbsp());

        int ppsId = b.ue();
        int spsId = b.ue();
        boolean entropicCodingMode = b.bit() == 1;
        boolean bottomFieldPicOrderInFramePresent = b.bit() == 1;
        int numSliceGroupsMinus1 = b.ue();
        if (numSliceGroupsMinus1 > 0) {
            throw new UnsupportedOperationException("slice groups are not supported by the minimal PPS reader");
        }

        int numRefIdxL0DefaultActiveMinus1 = b.ue();
        int numRefIdxL1DefaultActiveMinus1 = b.ue();
        boolean weightedPred = b.bit() == 1;
        int weightedBipredIdc = (int) b.u(2);
        int picInitQpMinus26 = b.se();
        int picInitQsMinus26 = b.se();
        int chromaQpIndexOffset = b.se();
        boolean deblockingFilterControlPresent = b.bit() == 1;
        boolean constrainedIntraPred = b.bit() == 1;
        boolean redundantPicCntPresent = b.bit() == 1;

        // The tail: the more_rbsp_data() fields (transform_8x8_mode_flag,
        // pps_scaling_matrix_present_flag, second_chroma_qp_index_offset) are present
        // when bits remain beyond the rbsp stop bit. The standard tail is tried first
        // and validated against the rbsp trailing bits that follow.
        int tailPos = b.bitsRead();
        boolean transform8x8Mode = false;
        int secondChromaQpIndexOffset = 0;
        try {
            if (b.bitsRemaining() > 1) { // more_rbsp_data()
                transform8x8Mode = b.bit() == 1; // transform_8x8_mode_flag
                if (b.bit() == 1) { // pps_scaling_matrix_present_flag
                    throw new UnsupportedOperationException("pps_scaling_list_data() is not supported by the minimal PPS reader");
                }
                secondChromaQpIndexOffset = b.se();
            }
            if (!_rbspTailClean(b)) {
                throw new IllegalStateException("PPS tail misaligned");
            }
        } catch (RuntimeException e) {
            b.skipTo(tailPos);
            // Fallback (validated against the rbsp trailing bits that follow): the
            // tail is not the H.264 one, so the fields are not decoded -- validate
            // that the stop bit + zero padding exist.
            boolean stopBit = false;
            while (b.hasBits(1)) {
                if (b.bit() == 1) {
                    stopBit = true;
                }
            }
            if (!stopBit) {
                throw new IllegalStateException("PPS rbsp_trailing_bits: no stop bit (walk is misaligned)");
            }
        }

        return new AVCPPS(
            ppsId,
            spsId,
            entropicCodingMode,
            bottomFieldPicOrderInFramePresent,
            numRefIdxL0DefaultActiveMinus1,
            numRefIdxL1DefaultActiveMinus1,
            weightedPred,
            weightedBipredIdc,
            picInitQpMinus26,
            picInitQsMinus26,
            chromaQpIndexOffset,
            deblockingFilterControlPresent,
            constrainedIntraPred,
            redundantPicCntPresent,
            transform8x8Mode,
            secondChromaQpIndexOffset
        );
    }

    @Override
    public String toString() {
        return String.format(
            "AVCPPS{ppsId=%d, spsId=%d, entropicCodingMode=%b, bottomFieldPicOrderInFramePresent=%b, numRefIdxL0DefaultActiveMinus1=%d, numRefIdxL1DefaultActiveMinus1=%d, weightedPred=%b, weightedBipredIdc=%d, picInitQp=%d, picInitQs=%d, chromaQpIndexOffset=%d, deblockingFilterControlPresent=%b, constrainedIntraPred=%b, redundantPicCntPresent=%b, transform8x8Mode=%b, secondChromaQpIndexOffset=%d}",
            this.ppsId,
            this.spsId,
            this.entropicCodingMode,
            this.bottomFieldPicOrderInFramePresent,
            this.numRefIdxL0DefaultActiveMinus1,
            this.numRefIdxL1DefaultActiveMinus1,
            this.weightedPred,
            this.weightedBipredIdc,
            this.picInitQpMinus26 + 26,
            this.picInitQsMinus26 + 26,
            this.chromaQpIndexOffset,
            this.deblockingFilterControlPresent,
            this.constrainedIntraPred,
            this.redundantPicCntPresent,
            this.transform8x8Mode,
            this.secondChromaQpIndexOffset
        );
    }

    // rbsp_trailing_bits: a stop bit, then zero padding.
    private static boolean _rbspTailClean(ASBitReader b) {
        if (!b.hasBits(1) || b.bit() != 1) {
            return false;
        }
        return b.bitsRemaining() == 0 || b.u(b.bitsRemaining()) == 0;
    }

}
