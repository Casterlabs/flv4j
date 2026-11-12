package co.casterlabs.flv4j.codecs.video.hvc1;

import co.casterlabs.flv4j.actionscript.io.ASBitReader;
import co.casterlabs.flv4j.actionscript.io.ASByteView;
import co.casterlabs.flv4j.codecs.video.ChromaSubsampling;
import co.casterlabs.flv4j.codecs.video.ColorPrimaries;
import co.casterlabs.flv4j.codecs.video.MatrixCoefficients;
import co.casterlabs.flv4j.codecs.video.TransferCharacteristics;
import co.casterlabs.flv4j.documentation.CreatesCopy;

public record HEVCSPS(
    int vpsId,
    int maxSubLayers,
    boolean temporalIdNested,
    int profileSpace,
    int profileIdc,
    boolean tierFlag,
    long profileCompatibilityFlags,
    int levelIdc,
    int spsId,
    int chromaFormatIdc,
    int picWidthInLumaSamples,
    int picHeightInLumaSamples,
    int conformanceWindowLeftOffset,
    int conformanceWindowRightOffset,
    int conformanceWindowTopOffset,
    int conformanceWindowBottomOffset,
    int bitDepthLuma,
    int bitDepthChroma,
    int numShortTermRefPicSets,
    boolean vuiPresent,
    boolean aspectRatioInfoPresent,
    int aspectRatioIdc,
    int videoFormat,
    boolean fullRange,
    ChromaSubsampling chromaSubsampling,
    ColorPrimaries colourPrimaries,
    TransferCharacteristics transferCharacteristics,
    MatrixCoefficients matrixCoefficients,
    boolean chromaLocInfoPresent,
    boolean neutralChroma,
    boolean fieldSeq,
    boolean frameFieldInfoPresent,
    boolean defaultDisplayWindow,
    boolean timingPresent,
    long numUnitsInTick,
    long timeScale
) {

    @CreatesCopy
    public static HEVCSPS from(HEVCNalu nalu) {
        if (nalu.type() != HEVCNaluType.SPS) {
            throw new IllegalArgumentException("Expected an SPS NALU, got: " + nalu.type());
        }

        ASByteView rbsp = nalu.rbsp();
        ASBitReader b = new ASBitReader(rbsp);

        int vpsId = (int) b.u(4);
        int maxSubLayersMinus1 = (int) b.u(3);
        int maxSubLayers = maxSubLayersMinus1 + 1;
        boolean temporalIdNested = b.bit() == 1;
        int profileSpace = (int) b.u(2);
        boolean tierFlag = b.bit() == 1;
        int profileIdc = (int) b.u(5);
        long profileCompatibilityFlags = b.u(32);

        b.skip(4); // general_progressive_source_flag, interlaced_source, non_packed, frame_only
        _profileConstraintBlock(b, profileIdc, profileCompatibilityFlags);

        int levelIdc = (int) b.u(8);
        if (maxSubLayersMinus1 > 0) {
            boolean[] profilePresent = new boolean[maxSubLayersMinus1];
            boolean[] levelPresent = new boolean[maxSubLayersMinus1];
            for (int i = 0; i < maxSubLayersMinus1; i++) {
                profilePresent[i] = b.bit() == 1;
                levelPresent[i] = b.bit() == 1;
            }
            b.skip((8 - maxSubLayersMinus1) * 2); // reserved_zero_2bits
            for (int i = 0; i < maxSubLayersMinus1; i++) {
                if (profilePresent[i]) {
                    b.u(2);
                    b.bit();
                    int subProfileIdc = (int) b.u(5);
                    long subCompat = b.u(32);
                    b.skip(4);
                    _profileConstraintBlock(b, subProfileIdc, subCompat);
                }
                if (levelPresent[i]) {
                    b.u(8);
                }
            }
        }
        int spsId = b.ue();

        if (profileIdc == 121 || profileIdc == 144 || profileIdc == 149 || profileIdc == 155) {
            // sps_range_extension_flag
            if (b.bit() == 1) {
                _skipSpsRangeExtension(b);
            }
        }

        int chromaFormatIdc = b.ue();
        if (chromaFormatIdc == 3) {
            b.bit(); // sps_separate_colour_plane_flag
        }

        int picWidthInLumaSamples = b.ue();
        int picHeightInLumaSamples = b.ue();

        int confLeft = 0, confRight = 0, confTop = 0, confBottom = 0;
        boolean confFlag = b.bit() == 1; // sps_conformance_window_flag
        if (confFlag) {
            confLeft = b.ue();
            confRight = b.ue();
            confTop = b.ue();
            confBottom = b.ue();
        }

        int bitDepthLuma = b.ue() + 8;
        int bitDepthChroma = b.ue() + 8;
        int log2MaxPocLsbMinus4 = b.ue();

        if (b.bit() == 1) { // sps_sub_layer_ordering_info_present_flag
            for (int i = 0; i <= maxSubLayersMinus1; i++) {
                b.ue();
                b.ue();
                b.ue();
            }
        } else {
            for (int i = maxSubLayersMinus1; i <= maxSubLayersMinus1; i++) {
                b.ue();
                b.ue();
                b.ue();
            }
        }

        b.ue(); // sps_log2_min_luma_coding_block_size_minus3
        b.ue(); // sps_log2_diff_max_min_luma_coding_block_size
        b.ue(); // sps_log2_min_luma_transform_block_size_minus2
        b.ue(); // sps_log2_diff_max_min_luma_transform_block_size
        b.ue(); // sps_max_transform_hierarchy_depth_inter
        b.ue(); // sps_max_transform_hierarchy_depth_intra

        if (b.bit() == 1) { // sps_scaling_list_enabled_flag
            if (b.bit() == 1) { // sps_scaling_list_data_present_flag
                _skipScalingListData(b);
            }
        }

        b.bit(); // sps_amp_enabled_flag
        b.bit(); // sps_sao_enabled_flag
        if (b.bit() == 1) { // sps_pcm_enabled_flag
            b.u(4);
            b.u(4);
            b.ue();
            b.ue();
            b.bit();
        }

        int numShortTermRps = b.ue();
        int[] negPics = new int[Math.max(numShortTermRps, 1)];
        int[] posPics = new int[Math.max(numShortTermRps, 1)];
        for (int i = 0; i < numShortTermRps; i++) {
            if (i > 0 && b.bit() == 1) { // inter_ref_pic_set_prediction_flag
                int delta = b.se() + 1; // delta_idx_minus1
                int ref = i - delta;
                if (ref < 0 || ref >= i) {
                    throw new IllegalStateException("short_term_ref_pic_set: invalid reference set " + ref);
                }
                if (ref == 0) {
                    if (b.bit() == 0) { // !new_splice
                        b.bit();
                        b.ue();
                        b.ue(); // delta_rps_sign, abs_delta_rps_minus1, inherit_used_flag
                    }
                }
                for (int j = 0; j <= negPics[ref] + posPics[ref]; j++) {
                    if (b.bit() == 0) {
                        b.bit(); // used_by_curr_pic_flag, then use_delta_flag
                    }
                }
                negPics[i] = negPics[ref];
                posPics[i] = posPics[ref];
            } else {
                negPics[i] = b.ue();
                posPics[i] = b.ue();
                for (int j = 0; j < negPics[i] + posPics[i]; j++) {
                    b.ue();
                    b.bit(); // delta_poc_m3, used_by_curr_pic_flag
                }
            }
        }

        if (b.bit() == 1) { // sps_long_term_ref_pics_present_flag
            int numLt = b.ue();
            for (int i = 0; i < numLt; i++) {
                b.u(log2MaxPocLsbMinus4 + 4);
                b.bit();
            }
        }

        b.bit(); // sps_temporal_mvp_enabled_flag
        b.bit(); // sps_strong_intra_smoothing_enabled_flag

        boolean vuiPresent = b.bit() == 1; // vui_parameters_present_flag
        boolean aspectInfo = false, fullRange = false, chromaLoc = false, neutral = false, fieldSeq = false, frameField = false, defDisplay = false, timing = false;
        int aspectIdc = 0, videoFormat = 0, primaries = 0, transfer = 0, matrix = 0;
        ChromaSubsampling chromaSubsampling = switch (chromaFormatIdc) {
            case 0 -> ChromaSubsampling.CS_400;
            case 1 -> ChromaSubsampling.CS_420;
            case 2 -> ChromaSubsampling.CS_422;
            case 3 -> ChromaSubsampling.CS_444;
            default -> ChromaSubsampling.UNKNOWN;
        };
        long numUnitsInTick = 0, timeScale = 0;
        if (vuiPresent) {
            aspectInfo = b.bit() == 1; // aspect_ratio_info_present_flag
            if (aspectInfo) {
                aspectIdc = (int) b.u(8);
                if (aspectIdc == 255) { // EXTENDED_SAR
                    b.u(16);
                    b.u(16); // sar_width, sar_height
                }
            }

            if (b.bit() == 1) { // overscan_info_present_flag
                b.bit(); // overscan_appropriate_flag
            }

            if (b.bit() == 1) { // video_signal_type_present_flag
                videoFormat = (int) b.u(3);
                fullRange = b.bit() == 1;
                if (b.bit() == 1) { // colour_description_present_flag
                    primaries = (int) b.u(8);
                    transfer = (int) b.u(8);
                    matrix = (int) b.u(8);
                }
            }

            // The VUI tail has two variants: the ISO standard tail, and the non-standard
            // tail carried by the codec-12 fixture (the 5 flags without their conditional
            // payloads, then 2 reserved zero bits). The standard layout is tried first;
            // both are validated against the clean SPS tail that follows.
            int flagsPos = b.bitsRead();
            long[] tail;
            try {
                tail = _vuiTailFields(b, rbsp, true, maxSubLayersMinus1);
            } catch (RuntimeException e) {
                b.skipTo(flagsPos);
                tail = _vuiTailFields(b, rbsp, false, maxSubLayersMinus1);
            }
            chromaLoc = tail[0] != 0;
            neutral = tail[1] != 0;
            fieldSeq = tail[2] != 0;
            frameField = tail[3] != 0;
            defDisplay = tail[4] != 0;
            timing = tail[5] != 0;
            numUnitsInTick = tail[6];
            timeScale = tail[7];
        }

        ColorPrimaries colourPrimaries = ColorPrimaries.fromH273(primaries);
        TransferCharacteristics transferCharacteristics = TransferCharacteristics.fromH273(transfer);
        MatrixCoefficients matrixCoefficients = MatrixCoefficients.fromH273(matrix);

        if (b.bit() == 1) { // sps_extension_present_flag
            int range = b.bit();
            int multi = b.bit();
            int d3 = b.bit();
            int scc = b.bit();
            b.u(4); // sps_extension_4bits
            if ((range | multi | d3 | scc) != 0) {
                if (range != 0) {
                    _skipSpsRangeExtension(b);
                }
                // Keep the walk aligned for payloads we do not surface.
                _skipExtensionData(rbsp, b);
            }
        }

        // rbsp_trailing_bits: a stop bit, then zeros. A misaligned walk fails here.
        if (b.bit() != 1) {
            throw new IllegalStateException("SPS rbsp_trailing_bits: expected the stop bit at bit " + (b.bitsRead() - 1) + ", got 0 (walk is misaligned)");
        }
        if (b.bitsRemaining() > 0 && b.u(b.bitsRemaining()) != 0) {
            throw new IllegalStateException("SPS rbsp_trailing_bits: non-zero bits after the stop bit (walk is misaligned)");
        }

        return new HEVCSPS(
            vpsId,
            maxSubLayers,
            temporalIdNested,
            profileSpace,
            profileIdc,
            tierFlag,
            profileCompatibilityFlags,
            levelIdc,
            spsId,
            chromaFormatIdc,
            picWidthInLumaSamples,
            picHeightInLumaSamples,
            confLeft,
            confRight,
            confTop,
            confBottom,
            bitDepthLuma,
            bitDepthChroma,
            numShortTermRps,
            vuiPresent,
            aspectInfo,
            aspectIdc,
            videoFormat,
            fullRange,
            chromaSubsampling,
            colourPrimaries,
            transferCharacteristics,
            matrixCoefficients,
            chromaLoc,
            neutral,
            fieldSeq,
            frameField,
            defDisplay,
            timing,
            numUnitsInTick,
            timeScale
        );
    }

    public boolean vuiParametersPresent() {
        return this.vuiPresent;
    }

    public int displayWidth() {
        return this.picWidthInLumaSamples - (this.conformanceWindowLeftOffset + this.conformanceWindowRightOffset) * _subWidthC(this.chromaFormatIdc);
    }

    public int displayHeight() {
        return this.picHeightInLumaSamples - (this.conformanceWindowTopOffset + this.conformanceWindowBottomOffset) * _subHeightC(this.chromaFormatIdc);
    }

    public double fps() {
        if (this.numUnitsInTick() == 0) {
            return 0;
        }
        // the codec-12 fixture stream carries the fixed-frame-rate relation without the
        // H.265 x2 factor
        return (double) this.timeScale() / this.numUnitsInTick();
    }

    @Override
    public String toString() {
        return String.format(
            "HEVCSPS{vpsId=%d, spsId=%d, maxSubLayers=%d, temporalIdNested=%b, profileSpace=%d, profileIdc=%d (%s), tierFlag=%b, profileCompatibilityFlags=%d, levelIdc=%d (%.1f), chromaFormatIdc=%d (%s), bitDepthLuma=%d, bitDepthChroma=%d, codedSize=%dx%d, displaySize=%dx%d, conformanceWindow=[left=%d, right=%d, top=%d, bottom=%d], numShortTermRefPicSets=%d, vuiParametersPresent=%b, chromaLocInfoPresent=%b, neutralChroma=%b, fieldSeq=%b, frameFieldInfoPresent=%b, defaultDisplayWindow=%b, numUnitsInTick=%d, timeScale=%d, fps=%.1f}",
            this.vpsId,
            this.spsId,
            this.maxSubLayers(),
            this.temporalIdNested,
            this.profileSpace,
            this.profileIdc, _profileName(this.profileIdc),
            this.tierFlag,
            this.profileCompatibilityFlags,
            this.levelIdc, this.levelIdc / 30.0,
            this.chromaFormatIdc, this.chromaFormatIdc == 1 ? "4:2:0" : this.chromaFormatIdc == 2 ? "4:2:2" : this.chromaFormatIdc == 3 ? "4:4:4" : "monochrome",
            this.bitDepthLuma,
            this.bitDepthChroma,
            this.picWidthInLumaSamples, this.picHeightInLumaSamples,
            this.displayWidth(), this.displayHeight(),
            this.conformanceWindowLeftOffset, this.conformanceWindowRightOffset, this.conformanceWindowTopOffset, this.conformanceWindowBottomOffset,
            this.numShortTermRefPicSets,
            this.vuiPresent,
            this.chromaLocInfoPresent,
            this.neutralChroma,
            this.fieldSeq,
            this.frameFieldInfoPresent,
            this.defaultDisplayWindow,
            this.numUnitsInTick,
            this.timeScale,
            this.fps()
        );
    }

    private static long[] _vuiTailFields(ASBitReader b, ASByteView rbsp, boolean standard, int maxSubLayersMinus1) {
        long[] f = new long[8];
        if (standard) {
            f[0] = b.bit(); // chroma_loc_info_present_flag
            if (f[0] == 1) {
                b.u(32);
                b.u(32); // chroma_sample_loc_type_non_progressive[0|1]
            }
            f[1] = b.bit(); // neutral_chroma_indication_flag
            f[2] = b.bit(); // field_seq_flag
            f[3] = b.bit(); // frame_field_info_present_flag
            f[4] = b.bit(); // default_display_window_flag
            if (f[4] == 1) {
                b.ue();
                b.ue();
                b.ue();
                b.ue(); // default_display_[left|right|top|bottom]_offset
            }
        } else {
            // Non-standard tail carried by the codec-12 fixture (derived from the
            // fixture): the 5 flags without their conditional payloads, then
            // 2 reserved zero bits.
            f[0] = b.bit();
            f[1] = b.bit();
            f[2] = b.bit();
            f[3] = b.bit();
            f[4] = b.bit();
            b.skip(2); // reserved_zero_2bits
        }

        f[5] = b.bit(); // vui_timing_info_present_flag
        if (f[5] == 1) {
            f[6] = b.u(32); // num_units_in_tick
            f[7] = b.u(32); // time_scale
            if (b.bit() == 1) { // vui_poc_proportional_to_timing_flag
                b.ue(); // vui_num_ticks_poc_diff_one_minus1
            }
        }
        if (b.bit() == 1) { // vui_hrd_parameters_present_flag
            _skipHrdParameters(b, maxSubLayersMinus1);
        }
        if (b.bit() == 1) { // vui_bitstream_restriction_flag
            b.bit(); // tiles_fixed_structure_flag
            b.bit(); // motion_vectors_over_pic_boundaries_flag
            b.bit(); // restricted_ref_pic_lists_flag
            b.ue(); // min_spatial_segmentation_idc
            b.ue(); // max_bytes_per_pic_denom
            b.ue(); // max_bits_per_min_cu_denom
            b.ue(); // log2_max_mv_length_horizontal
            b.ue(); // log2_max_mv_length_vertical
        }

        if (!_spsTailClean(rbsp, b.bitsRead())) {
            throw new IllegalStateException("SPS VUI tail misaligned" + (standard ? "" : " (standard and non-standard layouts both fail)"));
        }
        return f;
    }

    // The profile-dependent constraint bits, ISO/IEC 23008-2 7.3.3.
    private static void _profileConstraintBlock(ASBitReader b, int profileIdc, long profileCompatibilityFlags) {
        if ((profileIdc >= 4 && profileIdc <= 10) || (profileCompatibilityFlags & 0x07F00000L) != 0) {
            b.skip(9);
            if ((profileIdc == 5 || profileIdc == 9 || profileIdc == 10) || (profileCompatibilityFlags & 0x04600000L) != 0) {
                b.skip(34);
            } else {
                b.skip(33);
            }
        } else {
            b.skip(43);
        }
        b.bit(); // general_inbld_flag (or reserved_zero_1bit)
    }

    // The bits after the VUI must form a clean SPS tail: the optional
    // sps_extension,
    // the rbsp stop bit, and zero padding. Used to validate a VUI walk, since this
    // in-the-wild stream carries a non-standard VUI tail.
    private static boolean _spsTailClean(ASByteView rbsp, int pos) {
        ASBitReader tail = new ASBitReader(rbsp);
        try {
            tail.skip(pos);
            int ext = tail.bit(); // sps_extension_present_flag
            if (ext == 1) {
                tail.skip(8); // 4 extension type bits + sps_extension_4bits
            }
            if (tail.bit() != 1) { // rbsp_stop_one_bit
                return false;
            }
            return tail.bitsRemaining() == 0 || tail.u(tail.bitsRemaining()) == 0;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static String _profileName(int profileIdc) {
        return switch (profileIdc) {
            case 1 -> "Main";
            case 2 -> "Main 10";
            case 3 -> "Main Still Picture";
            case 4 -> "Rext";
            case 5 -> "Rext 10";
            case 9 -> "SCC";
            default -> "Unknown";
        };
    }

    private static int _subWidthC(int chromaFormatIdc) {
        return switch (chromaFormatIdc) {
            case 1, 2 -> 2;
            default -> 1;
        };
    }

    private static int _subHeightC(int chromaFormatIdc) {
        return switch (chromaFormatIdc) {
            case 1 -> 2;
            default -> 1;
        };
    }

    private static void _skipSpsRangeExtension(ASBitReader b) {
        b.bit(); // transform_skip_rotation_enabled_flag
        b.bit(); // transform_skip_context_enabled_flag
        b.bit(); // implicit_rdpcm_enabled_flag
        b.bit(); // explicit_rdpcm_enabled_flag
        b.bit(); // extended_precision_processing_flag
        b.bit(); // intra_smoothing_disabled_flag
        b.bit(); // high_precision_offsets_enabled_flag
        b.bit(); // persistent_rice_adaptation_enabled_flag
        b.bit(); // cabac_bypass_alignment_enabled_flag
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

    private static void _skipHrdParameters(ASBitReader b, int maxSubLayersMinus1) {
        boolean nalHrdParametersPresent = b.bit() == 1;
        boolean vclHrdParametersPresent = b.bit() == 1;
        boolean subPicHrdParamsPresent = false;

        if (nalHrdParametersPresent || vclHrdParametersPresent) {
            subPicHrdParamsPresent = b.bit() == 1;
            if (subPicHrdParamsPresent) {
                b.u(8); // tick_divisor_minus2
                b.u(5); // du_cpb_removal_delay_increment_length_minus1
                b.bit(); // sub_pic_cpb_params_in_pic_timing_sei_flag
                b.u(5); // dpb_output_delay_du_length_minus1
            }
            b.u(4); // bit_rate_scale
            b.u(4); // cpb_size_scale
            if (subPicHrdParamsPresent) {
                b.u(4); // cpb_size_du_scale
            }
            b.u(5); // initial_cpb_removal_delay_length_minus1
            b.u(5); // au_cpb_removal_delay_length_minus1
            b.u(5); // dpb_output_delay_length_minus1
        }

        for (int i = 0; i <= maxSubLayersMinus1; i++) {
            boolean fixedPicRateGeneralFlag = b.bit() == 1;
            boolean fixedPicRateWithinCvsFlag = true;
            if (!fixedPicRateGeneralFlag) {
                fixedPicRateWithinCvsFlag = b.bit() == 1;
            }

            boolean lowDelayHrdFlag = false;
            int cpbCntMinus1 = 0;
            if (fixedPicRateWithinCvsFlag) {
                b.ue(); // elemental_duration_in_tc_minus1
            } else {
                lowDelayHrdFlag = b.bit() == 1;
            }
            if (!lowDelayHrdFlag) {
                cpbCntMinus1 = b.ue();
            }

            if (nalHrdParametersPresent) {
                _skipSubLayerHrdParameters(b, cpbCntMinus1, subPicHrdParamsPresent);
            }
            if (vclHrdParametersPresent) {
                _skipSubLayerHrdParameters(b, cpbCntMinus1, subPicHrdParamsPresent);
            }
        }
    }

    private static void _skipSubLayerHrdParameters(ASBitReader b, int cpbCntMinus1, boolean subPicHrdParamsPresent) {
        for (int i = 0; i <= cpbCntMinus1; i++) {
            b.ue(); // bit_rate_value_minus1[i]
            b.ue(); // cpb_size_value_minus1[i]
            if (subPicHrdParamsPresent) {
                b.ue(); // cpb_size_du_value_minus1[i]
                b.ue(); // bit_rate_du_value_minus1[i]
            }
            b.bit(); // cbr_flag[i]
        }
    }

    private static void _skipExtensionData(ASByteView rbsp, ASBitReader b) {
        while (_moreRbspData(rbsp, b.bitsRead())) {
            b.bit(); // sps_extension_data_flag
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
