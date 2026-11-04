package co.casterlabs.flv4j.codecs.video.av01;

import co.casterlabs.flv4j.actionscript.io.ASBitReader;
import co.casterlabs.flv4j.codecs.video.ChromaSubsampling;
import co.casterlabs.flv4j.codecs.video.ColorPrimaries;
import co.casterlabs.flv4j.codecs.video.MatrixCoefficients;
import co.casterlabs.flv4j.codecs.video.TransferCharacteristics;

// https://aomediacodec.github.io/av1-spec/#sequence-header-obu-syntax
public record AV1SequenceHeader(
    int seqProfile,
    int seqLevelIdx0,
    int bitDepth,
    boolean monochrome,
    boolean fullRange,
    ChromaSubsampling chromaSubsampling,
    int chromaSamplePosition,
    ColorPrimaries colourPrimaries,
    TransferCharacteristics transferCharacteristics,
    MatrixCoefficients matrixCoefficients,
    boolean filmGrainParamsPresent
) {

    public static AV1SequenceHeader from(AV1OBU obu) {
        if (obu.type() != AV1OBUType.SEQUENCE_HEADER) {
            throw new IllegalArgumentException("Expected a sequence header OBU, got: " + obu.type());
        }
        return fromBits(new ASBitReader(obu.dataView()));
    }

    private static AV1SequenceHeader fromBits(ASBitReader b) {
        int seqProfile = (int) b.u(3);
        b.bit(); // still_picture
        boolean reducedStillPictureHeader = b.bit() == 1;

        int seqLevelIdx0;
        boolean decoderModelInfoPresentFlag = false;
        int operatingPointsCntMinus1 = 0;
        int decoderBufferDelayLengthMinus1 = 0;

        if (reducedStillPictureHeader) {
            seqLevelIdx0 = (int) b.u(5);
        } else {
            boolean timingInfoPresentFlag = b.bit() == 1;
            if (timingInfoPresentFlag) {
                b.u(32); // num_units_in_display_tick
                b.u(32); // time_scale
                boolean equalPictureInterval = b.bit() == 1;
                if (equalPictureInterval) {
                    readUvlc(b); // num_ticks_per_picture_minus_1
                }
                decoderModelInfoPresentFlag = b.bit() == 1;
                if (decoderModelInfoPresentFlag) {
                    decoderBufferDelayLengthMinus1 = (int) b.u(5);
                    b.u(32); // num_units_in_decoding_tick
                    b.u(5); // buffer_removal_time_length_minus_1
                    b.u(5); // frame_presentation_time_length_minus_1
                }
            }

            boolean initialDisplayDelayPresentFlag = b.bit() == 1;
            operatingPointsCntMinus1 = (int) b.u(5);
            seqLevelIdx0 = -1;

            for (int i = 0; i <= operatingPointsCntMinus1; i++) {
                b.u(12); // operating_point_idc
                int seqLevelIdx = (int) b.u(5);
                if (i == 0) {
                    seqLevelIdx0 = seqLevelIdx;
                }
                if (seqLevelIdx > 7) {
                    b.bit(); // seq_tier
                }

                if (decoderModelInfoPresentFlag) {
                    boolean decoderModelPresentForThisOp = b.bit() == 1;
                    if (decoderModelPresentForThisOp) {
                        int n = decoderBufferDelayLengthMinus1 + 1;
                        b.u(n); // decoder_buffer_delay
                        b.u(n); // encoder_buffer_delay
                        b.bit(); // low_delay_mode_flag
                    }
                }

                if (initialDisplayDelayPresentFlag) {
                    boolean initialDisplayDelayPresentForThisOp = b.bit() == 1;
                    if (initialDisplayDelayPresentForThisOp) {
                        b.u(4); // initial_display_delay_minus_1
                    }
                }
            }
        }

        int frameWidthBitsMinus1 = (int) b.u(4);
        int frameHeightBitsMinus1 = (int) b.u(4);
        b.u(frameWidthBitsMinus1 + 1); // max_frame_width_minus_1
        b.u(frameHeightBitsMinus1 + 1); // max_frame_height_minus_1

        boolean frameIdNumbersPresentFlag = false;
        if (!reducedStillPictureHeader) {
            frameIdNumbersPresentFlag = b.bit() == 1;
        }
        if (frameIdNumbersPresentFlag) {
            b.u(4); // delta_frame_id_length_minus_2
            b.u(3); // additional_frame_id_length_minus_1
        }

        b.bit(); // use_128x128_superblock
        b.bit(); // enable_filter_intra
        b.bit(); // enable_intra_edge_filter

        boolean enableOrderHint = false;
        if (!reducedStillPictureHeader) {
            b.bit(); // enable_interintra_compound
            b.bit(); // enable_masked_compound
            b.bit(); // enable_warped_motion
            b.bit(); // enable_dual_filter
            enableOrderHint = b.bit() == 1;
            if (enableOrderHint) {
                b.bit(); // enable_jnt_comp
                b.bit(); // enable_ref_frame_mvs
            }

            boolean seqChooseScreenContentTools = b.bit() == 1;
            int seqForceScreenContentTools;
            if (seqChooseScreenContentTools) {
                seqForceScreenContentTools = 2;
            } else {
                seqForceScreenContentTools = b.bit();
            }

            if (seqForceScreenContentTools > 0) {
                boolean seqChooseIntegerMv = b.bit() == 1;
                if (!seqChooseIntegerMv) {
                    b.bit(); // seq_force_integer_mv
                }
            }

            if (enableOrderHint) {
                b.u(3); // order_hint_bits_minus_1
            }
        }

        b.bit(); // enable_superres
        b.bit(); // enable_cdef
        b.bit(); // enable_restoration

        boolean highBitdepth = b.bit() == 1;
        boolean twelveBit = false;
        int bitDepth;
        if (seqProfile == 2 && highBitdepth) {
            twelveBit = b.bit() == 1;
            bitDepth = twelveBit ? 12 : 10;
        } else if (highBitdepth) {
            bitDepth = 10;
        } else {
            bitDepth = 8;
        }

        boolean monochrome = seqProfile != 1 && b.bit() == 1;

        int primaries = 2;
        int transfer = 2;
        int matrix = 2;
        boolean colourDescriptionPresentFlag = b.bit() == 1;
        if (colourDescriptionPresentFlag) {
            primaries = (int) b.u(8);
            transfer = (int) b.u(8);
            matrix = (int) b.u(8);
        }

        boolean fullRange;
        int chromaSamplePosition = 0;
        ChromaSubsampling chromaSubsampling;

        if (monochrome) {
            fullRange = b.bit() == 1;
            chromaSubsampling = ChromaSubsampling.CS_400;
        } else if (primaries == 1 && transfer == 13 && matrix == 0) {
            fullRange = true;
            chromaSubsampling = ChromaSubsampling.CS_444;
        } else {
            fullRange = b.bit() == 1;
            int subX;
            int subY;

            if (seqProfile == 0) {
                subX = 1;
                subY = 1;
            } else if (seqProfile == 1) {
                subX = 0;
                subY = 0;
            } else {
                if (bitDepth == 12) {
                    subX = b.bit();
                    if (subX == 1) {
                        subY = b.bit();
                    } else {
                        subY = 0;
                    }
                } else {
                    subX = 1;
                    subY = 0;
                }
            }

            chromaSubsampling = switch ((subX << 1) | subY) {
                case 0 -> ChromaSubsampling.CS_444;
                case 2 -> ChromaSubsampling.CS_422;
                case 3 -> ChromaSubsampling.CS_420;
                default -> ChromaSubsampling.UNKNOWN;
            };

            if (subX == 1 && subY == 1) {
                chromaSamplePosition = (int) b.u(2);
            }
        }

        b.bit(); // separate_uv_delta_q
        boolean filmGrainParamsPresent = b.bit() == 1;

        return new AV1SequenceHeader(
            seqProfile,
            seqLevelIdx0,
            bitDepth,
            monochrome,
            fullRange,
            chromaSubsampling,
            chromaSamplePosition,
            ColorPrimaries.fromH273(primaries),
            TransferCharacteristics.fromH273(transfer),
            MatrixCoefficients.fromH273(matrix),
            filmGrainParamsPresent
        );
    }

    private static int readUvlc(ASBitReader b) {
        int leadingZeros = 0;
        while (b.bit() == 0) {
            leadingZeros++;
            if (leadingZeros > 31) {
                throw new IllegalArgumentException("AV1 UVLC run of zeros exceeds 31");
            }
        }
        if (leadingZeros == 0) {
            return 0;
        }
        return (1 << leadingZeros) - 1 + (int) b.u(leadingZeros);
    }

}
