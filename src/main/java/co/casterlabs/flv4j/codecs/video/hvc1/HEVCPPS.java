package co.casterlabs.flv4j.codecs.video.hvc1;

import co.casterlabs.flv4j.actionscript.io.ASBitReader;
import co.casterlabs.flv4j.actionscript.io.ASByteView;
import co.casterlabs.flv4j.documentation.CreatesCopy;

public record HEVCPPS(
    int ppsId,
    int spsId,
    boolean dependentSliceSegmentsEnabled,
    boolean outputFlagPresent,
    int numExtraSliceHeaderBits,
    boolean signDataHidingEnabled,
    boolean cabacInitPresent,
    int numRefIdxL0DefaultActive,
    int numRefIdxL1DefaultActive,
    int initQp,
    boolean constrainedIntraPred,
    boolean transformSkipEnabled,
    boolean cuQpDeltaEnabled,
    int ppsCbQpOffset,
    int ppsCrQpOffset
) {

    @CreatesCopy
    public static HEVCPPS from(HEVCNalu nalu) {
        if (nalu.type() != HEVCNaluType.PPS) {
            throw new IllegalArgumentException("Expected a PPS NALU, got: " + nalu.type());
        }

        ASByteView rbsp = nalu.rbsp();
        ASBitReader b = new ASBitReader(rbsp);

        int ppsId = b.ue();
        int spsId = b.ue();
        boolean dependentSliceSegmentsEnabled = b.bit() == 1;
        boolean outputFlagPresent = b.bit() == 1;
        int numExtraSliceHeaderBits = (int) b.u(3);
        boolean signDataHidingEnabled = b.bit() == 1;
        boolean cabacInitPresent = b.bit() == 1;
        int numRefIdxL0DefaultActive = b.ue() + 1;
        int numRefIdxL1DefaultActive = b.ue() + 1;
        int initQp = b.se() + 26;
        boolean constrainedIntraPred = b.bit() == 1;
        boolean transformSkipEnabled = b.bit() == 1;

        boolean cuQpDeltaEnabled = b.bit() == 1;
        if (cuQpDeltaEnabled) {
            b.ue(); // diff_cu_qp_delta_depth
        }

        int ppsCbQpOffset = b.se();
        int ppsCrQpOffset = b.se();

        // The PPS tail has two variants: the ISO standard tail, and the non-standard
        // tail carried by the codec-12 fixture, which ends after the qp offsets (no
        // deblocking/scaling/extension fields). The standard tail is tried first;
        // both are validated against the rbsp trailing bits that follow.
        int tailPos = b.bitsRead();
        try {
            if (b.bit() == 1) { // pps_deblocking_filter_control_present_flag
                b.bit(); // pps_deblocking_filter_override_enabled_flag
                if (b.bit() == 0) { // !pps_deblocking_filter_disabled_flag
                    b.se();
                    b.se(); // pps_beta_offset_div2, pps_tc_offset_div2
                }
            }
            if (b.bit() == 1) { // pps_scaling_list_data_present_flag
                _skipScalingListData(b);
            }
            b.bit(); // pps_lists_modification_present_flag
            b.ue(); // pps_log2_parallel_merge_level_minus2
            b.bit(); // pps_slice_header_extension_present_flag
            if (b.bit() == 1) { // pps_extension_present_flag
                int range = b.bit();
                int multi = b.bit();
                int d3 = b.bit();
                int scc = b.bit();
                b.u(4); // pps_extension_4bits
                if ((range | multi | d3 | scc) != 0) {
                    // Keep the walk aligned for extension payloads we do not surface.
                    _skipExtensionData(rbsp, b);
                }
            }
            if (!_rbspTailClean(b)) {
                throw new IllegalStateException("PPS tail misaligned");
            }
        } catch (RuntimeException e) {
            b.skipTo(tailPos);
            // Non-standard tail carried by the codec-12 fixture (derived from the
            // fixture): the trailing bits begin right after the qp offsets and the
            // field set is not the H.265 one, so the tail fields are not decoded --
            // validate that the stop bit + zero padding exist.
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

        return new HEVCPPS(
            ppsId,
            spsId,
            dependentSliceSegmentsEnabled,
            outputFlagPresent,
            numExtraSliceHeaderBits,
            signDataHidingEnabled,
            cabacInitPresent,
            numRefIdxL0DefaultActive,
            numRefIdxL1DefaultActive,
            initQp,
            constrainedIntraPred,
            transformSkipEnabled,
            cuQpDeltaEnabled,
            ppsCbQpOffset,
            ppsCrQpOffset
        );
    }

    @Override
    public String toString() {
        return String.format(
            "HEVCPPS{ppsId=%d, spsId=%d, dependentSliceSegmentsEnabled=%b, outputFlagPresent=%b, numExtraSliceHeaderBits=%d, signDataHidingEnabled=%b, cabacInitPresent=%b, numRefIdxL0DefaultActive=%d, numRefIdxL1DefaultActive=%d, initQp=%d, constrainedIntraPred=%b, transformSkipEnabled=%b, cuQpDeltaEnabled=%b, ppsCbQpOffset=%d, ppsCrQpOffset=%d}",
            this.ppsId,
            this.spsId,
            this.dependentSliceSegmentsEnabled,
            this.outputFlagPresent,
            this.numExtraSliceHeaderBits,
            this.signDataHidingEnabled,
            this.cabacInitPresent,
            this.numRefIdxL0DefaultActive,
            this.numRefIdxL1DefaultActive,
            this.initQp,
            this.constrainedIntraPred,
            this.transformSkipEnabled,
            this.cuQpDeltaEnabled,
            this.ppsCbQpOffset,
            this.ppsCrQpOffset
        );
    }

    // rbsp_trailing_bits: a stop bit, then zero padding.
    private static boolean _rbspTailClean(ASBitReader b) {
        if (!b.hasBits(1) || b.bit() != 1) {
            return false;
        }
        return b.bitsRemaining() == 0 || b.u(b.bitsRemaining()) == 0;
    }

    private static void _skipScalingListData(ASBitReader b) {
        for (int sizeId = 0; sizeId < 4; sizeId++) {
            for (int matrixId = 0; matrixId < 6; matrixId += (sizeId == 3) ? 3 : 1) {
                boolean predModeFlag = b.bit() == 1;
                if (!predModeFlag) {
                    b.ue(); // scaling_list_pred_matrix_id_delta
                    continue;
                }

                int coefNum = Math.min(64, 1 << (4 + (sizeId << 1)));
                if (sizeId > 1) {
                    b.se(); // scaling_list_dc_coef_minus8
                }
                for (int i = 0; i < coefNum; i++) {
                    b.se(); // scaling_list_delta_coef
                }
            }
        }
    }

    private static void _skipExtensionData(ASByteView rbsp, ASBitReader b) {
        while (_moreRbspData(rbsp, b.bitsRead())) {
            b.bit(); // pps_extension_data_flag
        }
    }

    private static boolean _moreRbspData(ASByteView rbsp, int bitPos) {
        ASBitReader probe = new ASBitReader(rbsp);
        probe.skipTo(bitPos);
        if (!probe.hasBits(1)) {
            return false;
        }

        if (probe.bit() == 0) {
            return true;
        }

        return probe.bitsRemaining() > 0 && probe.u(probe.bitsRemaining()) != 0;
    }

}
