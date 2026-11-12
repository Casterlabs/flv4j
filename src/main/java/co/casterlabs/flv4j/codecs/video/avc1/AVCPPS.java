package co.casterlabs.flv4j.codecs.video.avc1;

import co.casterlabs.flv4j.actionscript.io.ASBitReader;
import co.casterlabs.flv4j.actionscript.io.ASByteView;
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

        ASByteView rbsp = nalu.rbsp();
        ASBitReader b = new ASBitReader(rbsp);

        int ppsId = b.ue();
        int spsId = b.ue();
        boolean entropicCodingMode = b.bit() == 1;
        boolean bottomFieldPicOrderInFramePresent = b.bit() == 1;
        int numSliceGroupsMinus1 = b.ue();
        if (numSliceGroupsMinus1 > 0) {
            _skipSliceGroups(b, numSliceGroupsMinus1);
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
                    int scalingPos = b.bitsRead();
                    int[] parsedTail = _parsePpsScalingTail(rbsp, scalingPos, transform8x8Mode);
                    if (parsedTail == null) {
                        throw new IllegalStateException("PPS scaling_list_data tail misaligned");
                    }
                    b.skipTo(parsedTail[0]);
                    secondChromaQpIndexOffset = parsedTail[1];
                } else {
                    secondChromaQpIndexOffset = b.se();
                }
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

    private static int[] _parsePpsScalingTail(ASByteView rbsp, int scalingDataPos, boolean transform8x8Mode) {
        int[] candidates = transform8x8Mode ? new int[] { 8, 12 } : new int[] { 6 };
        for (int listCount : candidates) {
            ASBitReader probe = new ASBitReader(rbsp);
            probe.skipTo(scalingDataPos);
            try {
                _skipPpsScalingListData(probe, listCount);
                int secondChromaQpIndexOffset = probe.se();
                int posAfterSecond = probe.bitsRead();
                if (_rbspTailClean(probe)) {
                    return new int[] { posAfterSecond, secondChromaQpIndexOffset };
                }
            } catch (RuntimeException ignored) {}
        }
        return null;
    }

    private static void _skipPpsScalingListData(ASBitReader b, int listCount) {
        for (int i = 0; i < listCount; i++) {
            if (b.bit() == 1) { // pps_scaling_list_present_flag[i]
                _skipScalingList(b, i < 6 ? 16 : 64);
            }
        }
    }

    private static void _skipScalingList(ASBitReader b, int listSize) {
        int lastScale = 8;
        int nextScale = 8;

        for (int j = 0; j < listSize; j++) {
            if (nextScale != 0) {
                int deltaScale = b.se();
                nextScale = (lastScale + deltaScale + 256) % 256;
            }
            if (nextScale != 0) {
                lastScale = nextScale;
            }
        }
    }

    private static void _skipSliceGroups(ASBitReader b, int numSliceGroupsMinus1) {
        int sliceGroupMapType = b.ue();

        switch (sliceGroupMapType) {
            case 0 -> {
                for (int i = 0; i <= numSliceGroupsMinus1; i++) {
                    b.ue(); // run_length_minus1[i]
                }
            }
            case 2 -> {
                for (int i = 0; i < numSliceGroupsMinus1; i++) {
                    b.ue(); // top_left[i]
                    b.ue(); // bottom_right[i]
                }
            }
            case 3, 4, 5 -> {
                b.bit(); // slice_group_change_direction_flag
                b.ue(); // slice_group_change_rate_minus1
            }
            case 6 -> {
                int picSizeInMapUnitsMinus1 = b.ue();
                int bitsPerId = _ceilLog2(numSliceGroupsMinus1 + 1);
                for (int i = 0; i <= picSizeInMapUnitsMinus1; i++) {
                    b.u(bitsPerId); // slice_group_id[i]
                }
            }
            default -> {
                // map type 1 has no extra payload in PPS
            }
        }
    }

    private static int _ceilLog2(int value) {
        int bits = 0;
        int threshold = 1;
        while (threshold < value) {
            threshold <<= 1;
            bits++;
        }
        return bits;
    }

}
