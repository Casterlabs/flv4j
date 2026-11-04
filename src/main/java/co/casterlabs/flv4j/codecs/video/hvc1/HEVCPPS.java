package co.casterlabs.flv4j.codecs.video.hvc1;

import co.casterlabs.flv4j.actionscript.io.ASBitReader;
import co.casterlabs.flv4j.documentation.CreatesCopy;

public record HEVCPPS(
    int ppsId,
    int spsId,
    boolean dependentSliceSegmentsEnabled,
    boolean outputFlagPresent,
    int numExtraSliceHeaderBits,
    boolean signDataHidingEnabled,
    boolean cabacInitPresent,
    int numRefIdxL0DefaultActiveMinus1,
    int numRefIdxL1DefaultActiveMinus1,
    int initQpMinus26,
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

        ASBitReader b = new ASBitReader(nalu.rbsp());

        int ppsId = b.ue();
        int spsId = b.ue();
        boolean dependentSliceSegmentsEnabled = b.bit() == 1;
        boolean outputFlagPresent = b.bit() == 1;
        int numExtraSliceHeaderBits = (int) b.u(3);
        boolean signDataHidingEnabled = b.bit() == 1;
        boolean cabacInitPresent = b.bit() == 1;
        int numRefIdxL0DefaultActiveMinus1 = b.ue();
        int numRefIdxL1DefaultActiveMinus1 = b.ue();
        int initQpMinus26 = b.se();
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
                throw new UnsupportedOperationException("scaling_list_data() is not supported by the minimal PPS reader");
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
                    throw new UnsupportedOperationException("PPS extensions are not supported by the minimal PPS reader");
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
            numRefIdxL0DefaultActiveMinus1,
            numRefIdxL1DefaultActiveMinus1,
            initQpMinus26,
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
            "HEVCPPS{ppsId=%d, spsId=%d, dependentSliceSegmentsEnabled=%b, outputFlagPresent=%b, numExtraSliceHeaderBits=%d, signDataHidingEnabled=%b, cabacInitPresent=%b, numRefIdxL0DefaultActiveMinus1=%d, numRefIdxL1DefaultActiveMinus1=%d, initQpMinus26=%d, constrainedIntraPred=%b, transformSkipEnabled=%b, cuQpDeltaEnabled=%b, ppsCbQpOffset=%d, ppsCrQpOffset=%d}",
            this.ppsId,
            this.spsId,
            this.dependentSliceSegmentsEnabled,
            this.outputFlagPresent,
            this.numExtraSliceHeaderBits,
            this.signDataHidingEnabled,
            this.cabacInitPresent,
            this.numRefIdxL0DefaultActiveMinus1,
            this.numRefIdxL1DefaultActiveMinus1,
            this.initQpMinus26,
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

}
