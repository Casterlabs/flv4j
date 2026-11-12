package co.casterlabs.flv4j.test.flv.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import co.casterlabs.flv4j.codecs.video.avc1.AVCDecoderConfigurationRecord;
import co.casterlabs.flv4j.codecs.video.avc1.AVCNaluType;
import co.casterlabs.flv4j.codecs.video.avc1.AVCNalus;
import co.casterlabs.flv4j.codecs.video.avc1.AVCPPS;
import co.casterlabs.flv4j.codecs.video.avc1.AVCSPS;
import co.casterlabs.flv4j.codecs.video.avc1.AVCVideoData;
import co.casterlabs.flv4j.codecs.video.ChromaSubsampling;
import co.casterlabs.flv4j.codecs.video.ColorPrimaries;
import co.casterlabs.flv4j.codecs.video.MatrixCoefficients;
import co.casterlabs.flv4j.codecs.video.TransferCharacteristics;
import co.casterlabs.flv4j.flv.FLVFileHeader;
import co.casterlabs.flv4j.flv.muxing.NonSeekableFLVDemuxer;
import co.casterlabs.flv4j.flv.tags.FLVTag;
import co.casterlabs.flv4j.flv.tags.video.FLVStandardVideoTagData;
import co.casterlabs.flv4j.flv.tags.video.FLVVideoCodec;
import co.casterlabs.flv4j.test.Media;

public class H264 {

    private void videoTest(String name, FLVVideoCodec expectedCodec) throws IOException {
        boolean[] foundTag = {
                false
        }; // pointer hax

        new NonSeekableFLVDemuxer() {
            @Override
            protected void onHeader(FLVFileHeader header) {}

            @Override
            protected void onTag(long previousTagSize, FLVTag tag) {
                if (tag.data() instanceof FLVStandardVideoTagData data) {
                    foundTag[0] = true;
                    assertTrue(data.codec() == expectedCodec, "Failed to find expected codec: " + expectedCodec + " in " + name + ". got: " + data.codec());
                }
            }

            @Override
            protected void onEnd() {}
        }.start(Media.stream(name));

        assertTrue(foundTag[0], "Did not find any video tags in " + name);
    }

    @Test
    public void standard_H264() throws IOException {
        videoTest("standard/video/h264", FLVVideoCodec.H264);
    }

    @Test
    public void standard_H264_structure() throws IOException {
        boolean[] foundSeqHeader = {
                false
        };

        new NonSeekableFLVDemuxer() {
            @Override
            protected void onHeader(FLVFileHeader header) {}

            @Override
            protected void onTag(long previousTagSize, FLVTag tag) {
                if (tag.data() instanceof FLVStandardVideoTagData data && data.codec() == FLVVideoCodec.H264
                    && data.data() instanceof AVCVideoData avc
                    && avc.isSequenceHeader() && avc.packetData() instanceof AVCDecoderConfigurationRecord record) {
                    foundSeqHeader[0] = true;

                    // ffprobe ground truth (standard/video/h264.flv): h264 High,
                    // level 1.2 (level_idc 12), display 320x180, yuv420p, 24/1.
                    assertEquals(1, record.configurationVersion());
                    assertEquals(100, record.profileIndication()); // High
                    assertEquals(0, record.profileCompatibility());
                    assertEquals(12, record.levelIndication()); // level 1.2 (level_idc 12)
                    assertEquals(4, record.lengthSize());
                    assertEquals(1, record.numOfSPS());
                    assertEquals(1, record.numOfPPS());

                    // The NALUs: SPS (26B), PPS (6B) (avcC ground truth).
                    assertEquals(26, record.sps()[0].view().length());
                    assertEquals(6, record.pps()[0].view().length());

                    AVCSPS sps = AVCSPS.from(record.sps()[0]);
                    assertEquals(100, sps.profileIdc()); // High
                    assertEquals(12, sps.levelIdc());
                    assertEquals(0, sps.spsId());
                    assertEquals(1, sps.chromaFormatIdc()); // 4:2:0
                    assertEquals(8, sps.bitDepthLuma());
                    assertEquals(8, sps.bitDepthChroma());
                    assertEquals(4, sps.maxNumRefFrames());
                    assertEquals(20, sps.picWidthInMbs());
                    assertEquals(12, sps.picHeightInMapUnits());
                    assertTrue(sps.frameMbsOnly());
                    assertEquals(320, sps.codedWidth());
                    assertEquals(192, sps.codedHeight()); // crop bottom 6 (chroma) = 12 luma rows
                    assertEquals(320, sps.displayWidth());
                    assertEquals(180, sps.displayHeight());
                    assertEquals(0, sps.cropLeftOffset());
                    assertEquals(0, sps.cropRightOffset());
                    assertEquals(0, sps.cropTopOffset());
                    assertEquals(6, sps.cropBottomOffset());
                    assertTrue(sps.vuiPresent());
                    assertEquals(ChromaSubsampling.CS_420, sps.chromaSubsampling());
                    assertEquals(ColorPrimaries.RESERVED, sps.colourPrimaries());
                    assertEquals(TransferCharacteristics.RESERVED, sps.transferCharacteristics());
                    assertEquals(MatrixCoefficients.IDENTITY, sps.matrixCoefficients());
                    assertTrue(sps.timingPresent());
                    assertEquals(1, sps.numUnitsInTick());
                    assertEquals(48, sps.timeScale()); // 48 / 1 / 2 = 24fps

                    AVCPPS pps = AVCPPS.from(record.pps()[0]);
                    assertEquals(0, pps.ppsId());
                    assertEquals(0, pps.spsId());
                    assertTrue(pps.entropicCodingMode()); // CABAC
                    assertFalse(pps.bottomFieldPicOrderInFramePresent());
                    assertEquals(3, pps.numRefIdxL0DefaultActive());
                    assertEquals(1, pps.numRefIdxL1DefaultActive());
                    assertTrue(pps.weightedPred());
                    assertEquals(2, pps.weightedBipredIdc());
                    assertEquals(23, pps.picInitQp());
                    assertEquals(26, pps.picInitQs());
                    assertEquals(-2, pps.chromaQpIndexOffset());
                    assertTrue(pps.deblockingFilterControlPresent());
                    assertFalse(pps.constrainedIntraPred());
                    assertFalse(pps.redundantPicCntPresent());
                    assertTrue(pps.transform8x8Mode()); // 8x8DCT
                    assertEquals(-2, pps.secondChromaQpIndexOffset());
                }
            }

            @Override
            protected void onEnd() {}
        }.start(Media.stream("standard/video/h264"));

        assertTrue(foundSeqHeader[0], "Did not find an H.264 sequence header");
    }

    @Test
    public void standard_H264_coded_frames() throws IOException {
        List<AVCNalus> naluFrames = new ArrayList<>();
        int[] keyframes = {
                0
        };

        new NonSeekableFLVDemuxer() {
            @Override
            protected void onHeader(FLVFileHeader header) {}

            @Override
            protected void onTag(long previousTagSize, FLVTag tag) {
                if (tag.data() instanceof FLVStandardVideoTagData data && data.codec() == FLVVideoCodec.H264
                    && data.data() instanceof AVCVideoData avc
                    && avc.packetData() instanceof AVCNalus list) {
                    naluFrames.add(list);
                    if (data.frameType().id == 1) {
                        keyframes[0]++;
                    }
                }
            }

            @Override
            protected void onEnd() {}
        }.start(Media.stream("standard/video/h264"));

        // ffprobe ground truth (standard/video/h264.flv): 240 frames/packets,
        // 1 keyframe (K__).
        assertEquals(240, naluFrames.size());
        assertEquals(1, keyframes[0]);

        // First frame: the SEI prefix + the IDR slice.
        assertEquals(2, naluFrames.get(0).numberOfNalus());
        assertEquals(AVCNaluType.SEI_PARAMETERS, naluFrames.get(0).nalus()[0].type());
        assertEquals(686, naluFrames.get(0).nalus()[0].view().length());
        assertEquals(AVCNaluType.CODED_SLICE_IDR_PICTURE, naluFrames.get(0).nalus()[1].type());
        assertEquals(64, naluFrames.get(0).nalus()[1].view().length());

        // Second frame: a non-IDR slice.
        assertEquals(1, naluFrames.get(1).numberOfNalus());
        assertEquals(AVCNaluType.CODED_SLICE_NON_IDR_PICTURE, naluFrames.get(1).nalus()[0].type());
        assertEquals(177, naluFrames.get(1).nalus()[0].view().length());
    }

}
