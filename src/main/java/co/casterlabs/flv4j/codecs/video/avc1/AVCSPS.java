package co.casterlabs.flv4j.codecs.video.avc1;

import co.casterlabs.flv4j.actionscript.io.ASBitReader;
import co.casterlabs.flv4j.codecs.video.ChromaSubsampling;
import co.casterlabs.flv4j.codecs.video.ColorPrimaries;
import co.casterlabs.flv4j.codecs.video.MatrixCoefficients;
import co.casterlabs.flv4j.codecs.video.TransferCharacteristics;
import co.casterlabs.flv4j.documentation.CreatesCopy;

public record AVCSPS(
    int profileIdc,
    int levelIdc,
    int spsId,
    int chromaFormatIdc,
    boolean separateColourPlane,
    int bitDepthLumaMinus8,
    int bitDepthChromaMinus8,
    int log2MaxFrameNumMinus4,
    int picOrderCntType,
    int maxNumRefFrames,
    boolean gapsInFrameNumValueAllowed,
    int picWidthInMbsMinus1,
    int picHeightInMapUnitsMinus1,
    boolean frameMbsOnly,
    int cropLeftOffset,
    int cropRightOffset,
    int cropTopOffset,
    int cropBottomOffset,
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
    boolean timingPresent,
    long numUnitsInTick,
    long timeScale,
    boolean picStructPresent,
    boolean bitstreamRestrictionPresent
) {

    @CreatesCopy
    public static AVCSPS from(AVCNalu nalu) {
        if (nalu.type() != AVCNaluType.SPS) {
            throw new IllegalArgumentException("Expected an SPS NALU, got: " + nalu.type());
        }
        ASBitReader b = new ASBitReader(nalu.rbsp());

        int profileIdc = (int) b.u(8);
        b.skip(8); // constraint_set[0-5]_flags + reserved_zero_2bits
        int levelIdc = (int) b.u(8);
        int spsId = b.ue();

        int chromaFormatIdc = 0;
        boolean separateColourPlane = false;
        int bitDepthLumaMinus8 = 0, bitDepthChromaMinus8 = 0;
        if (_highProfileSps(profileIdc)) {
            chromaFormatIdc = b.ue();
            if (chromaFormatIdc == 3) {
                separateColourPlane = b.bit() == 1; // separate_colour_plane_flag
            }
            bitDepthLumaMinus8 = b.ue();
            bitDepthChromaMinus8 = b.ue();
            b.bit(); // qpprime_y_zero_transform_bypass_flag
            if (b.bit() == 1) { // seq_scaling_matrix_present_flag
                _skipSeqScalingListData(b, chromaFormatIdc);
            }
        }

        int log2MaxFrameNumMinus4 = b.ue();
        int picOrderCntType = b.ue();
        if (picOrderCntType == 0) {
            b.ue(); // log2_max_pic_order_cnt_lsb_minus4
        } else if (picOrderCntType == 1) {
            b.bit(); // delta_pic_order_always_zero_flag
            b.se(); // offset_for_non_ref_pic
            b.se(); // offset_for_top_to_bottom_field
            int numRefFramesInPocCycle = b.ue();
            for (int i = 0; i < numRefFramesInPocCycle; i++) {
                b.se(); // offset_for_ref_frame[i]
            }
        }

        int maxNumRefFrames = b.ue();
        boolean gapsInFrameNumValueAllowed = b.bit() == 1;

        int picWidthInMbsMinus1 = b.ue();
        int picHeightInMapUnitsMinus1 = b.ue();
        boolean frameMbsOnly = b.bit() == 1;
        if (!frameMbsOnly) {
            b.bit(); // mb_adaptive_frame_field_flag
        }
        b.bit(); // direct_8x8_inference_flag

        int cropLeft = 0, cropRight = 0, cropTop = 0, cropBottom = 0;
        if (b.bit() == 1) { // frame_cropping_flag
            cropLeft = b.ue();
            cropRight = b.ue();
            cropTop = b.ue();
            cropBottom = b.ue();
        }

        boolean vuiPresent = b.bit() == 1; // vui_parameters_present_flag
        boolean aspectInfo = false, fullRange = false, chromaLoc = false, timing = false, picStruct = false, brs = false;
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
                fullRange = b.bit() == 1; // video_full_range_flag
                if (b.bit() == 1) { // colour_description_present_flag
                    primaries = (int) b.u(8);
                    transfer = (int) b.u(8);
                    matrix = (int) b.u(8);
                }
            }

            chromaLoc = b.bit() == 1; // chroma_loc_info_present_flag
            if (chromaLoc) {
                b.ue();
                b.ue(); // chroma_sample_loc_type_top/bottom_field

                // NB: the H.264 VUI has no neutral_chroma / field_seq /
                // frame_field_info / default_display_window flags (those are
                // H.265-only) -- the timing info comes next.
            }

            timing = b.bit() == 1; // vui_timing_info_present_flag
            if (timing) {
                numUnitsInTick = b.u(32);
                timeScale = b.u(32);
                if (b.bit() == 1) { // vui_poc_proportional_to_timing_flag
                    b.ue(); // vui_num_ticks_poc_diff_one_minus1
                }
            }

            boolean nalHrdPresent = b.bit() == 1; // vui_nal_hrd_parameters_present_flag
            if (nalHrdPresent) {
                _skipHrdParameters(b);
            }

            boolean vclHrdPresent = b.bit() == 1; // vui_vcl_hrd_parameters_present_flag
            if (vclHrdPresent) {
                _skipHrdParameters(b);
            }

            if (nalHrdPresent || vclHrdPresent) {
                b.bit(); // low_delay_hrd_flag
            }

            picStruct = b.bit() == 1; // pic_struct_present_flag
            brs = b.bit() == 1; // vui_bitstream_restriction_flag
            if (brs) {
                b.bit(); // motion_vectors_over_pic_boundaries_flag
                b.ue(); // max_bytes_per_pic_denom
                b.ue(); // max_bits_per_mb_denom
                b.ue(); // log2_max_mv_length_horizontal
                b.ue(); // log2_max_mv_length_vertical
                b.ue(); // log2_max_frame_num_minus4_restriction
                b.ue(); // max_dec_frame_buffering
            }
        }

        ColorPrimaries colourPrimaries = ColorPrimaries.fromH273(primaries);
        TransferCharacteristics transferCharacteristics = TransferCharacteristics.fromH273(transfer);
        MatrixCoefficients matrixCoefficients = MatrixCoefficients.fromH273(matrix);

        // rbsp_trailing_bits: a stop bit, then zeros. A misaligned walk fails here.
        if (b.bit() != 1) {
            throw new IllegalStateException("SPS rbsp_trailing_bits: expected the stop bit at bit " + (b.bitsRead() - 1) + ", got 0 (walk is misaligned)");
        }
        if (b.bitsRemaining() > 0 && b.u(b.bitsRemaining()) != 0) {
            throw new IllegalStateException("SPS rbsp_trailing_bits: non-zero bits after the stop bit (walk is misaligned)");
        }

        return new AVCSPS(
            profileIdc,
            levelIdc,
            spsId,
            chromaFormatIdc,
            separateColourPlane,
            bitDepthLumaMinus8,
            bitDepthChromaMinus8,
            log2MaxFrameNumMinus4,
            picOrderCntType,
            maxNumRefFrames,
            gapsInFrameNumValueAllowed,
            picWidthInMbsMinus1,
            picHeightInMapUnitsMinus1,
            frameMbsOnly,
            cropLeft,
            cropRight,
            cropTop,
            cropBottom,
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
            timing,
            numUnitsInTick,
            timeScale,
            picStruct,
            brs
        );
    }

    public int codedWidth() {
        return (this.picWidthInMbsMinus1 + 1) * 16;
    }

    public int codedHeight() {
        return (this.frameMbsOnly ? 1 : 2) * (this.picHeightInMapUnitsMinus1 + 1) * 16;
    }

    public int displayWidth() {
        return this.codedWidth() - (this.cropLeftOffset + this.cropRightOffset) * _subWidthC(this.chromaFormatIdc);
    }

    public int displayHeight() {
        return this.codedHeight() - (this.cropTopOffset + this.cropBottomOffset) * _subHeightC(this.chromaFormatIdc);
    }

    public double fps() {
        if (this.numUnitsInTick() == 0) {
            return 0;
        }
        // x2 factor of the H.264 VUI, ISO/IEC 14496-10 7.3.2.1.1
        return (double) this.timeScale() / this.numUnitsInTick() / 2;
    }

    @Override
    public String toString() {
        return String.format(
            "AVCSPS{profileIdc=%d (%s), levelIdc=%d (%.1f), spsId=%d, chromaFormatIdc=%d (%s), bitDepthLuma=%d, bitDepthChroma=%d, picOrderCntType=%d, maxNumRefFrames=%d, codedSize=%dx%d, displaySize=%dx%d, crop=[left=%d, right=%d, top=%d, bottom=%d], vuiPresent=%b, fullRange=%b, numUnitsInTick=%d, timeScale=%d, fps=%.1f}",
            this.profileIdc, _profileName(this.profileIdc),
            this.levelIdc, this.levelIdc / 10.0,
            this.spsId,
            this.chromaFormatIdc, this.chromaFormatIdc == 1 ? "4:2:0" : this.chromaFormatIdc == 2 ? "4:2:2" : this.chromaFormatIdc == 3 ? "4:4:4" : "monochrome",
            this.bitDepthLumaMinus8 + 8,
            this.bitDepthChromaMinus8 + 8,
            this.picOrderCntType,
            this.maxNumRefFrames,
            this.codedWidth(), this.codedHeight(),
            this.displayWidth(), this.displayHeight(),
            this.cropLeftOffset, this.cropRightOffset, this.cropTopOffset, this.cropBottomOffset,
            this.vuiPresent,
            this.fullRange,
            this.numUnitsInTick,
            this.timeScale,
            this.fps()
        );
    }

    private static boolean _highProfileSps(int profileIdc) {
        return profileIdc == 100 || profileIdc == 110 || profileIdc == 122 || profileIdc == 244
            || profileIdc == 44 || profileIdc == 83 || profileIdc == 86
            || profileIdc == 118 || profileIdc == 128 || profileIdc == 138
            || profileIdc == 144;
    }

    private static String _profileName(int profileIdc) {
        return switch (profileIdc) {
            case 10 -> "Main 10";
            case 11 -> "Main Still Picture";
            case 44 -> "CAVLC 4:4:4 Intra";
            case 66 -> "Baseline";
            case 77 -> "Main";
            case 88 -> "Extended";
            case 100 -> "High";
            case 110 -> "High 10";
            case 118 -> "High 10 Intra";
            case 122 -> "High 4:2:2";
            case 128 -> "Multiview High";
            case 144 -> "High 4:4:4 Intra";
            case 145 -> "High 4:4:4 Predictive";
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

    private static void _skipSeqScalingListData(ASBitReader b, int chromaFormatIdc) {
        int listCount = chromaFormatIdc == 3 ? 12 : 8;
        for (int i = 0; i < listCount; i++) {
            if (b.bit() == 1) { // seq_scaling_list_present_flag[i]
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

    private static void _skipHrdParameters(ASBitReader b) {
        int cpbCntMinus1 = b.ue();
        b.u(4); // bit_rate_scale
        b.u(4); // cpb_size_scale

        for (int schedSelIdx = 0; schedSelIdx <= cpbCntMinus1; schedSelIdx++) {
            b.ue(); // bit_rate_value_minus1[schedSelIdx]
            b.ue(); // cpb_size_value_minus1[schedSelIdx]
            b.bit(); // cbr_flag[schedSelIdx]
        }

        b.u(5); // initial_cpb_removal_delay_length_minus1
        b.u(5); // cpb_removal_delay_length_minus1
        b.u(5); // dpb_output_delay_length_minus1
        b.u(5); // time_offset_length
    }

}
