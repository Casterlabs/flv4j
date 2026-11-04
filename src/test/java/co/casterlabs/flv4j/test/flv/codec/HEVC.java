package co.casterlabs.flv4j.test.flv.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import co.casterlabs.flv4j.actionscript.io.ASByteView;
import co.casterlabs.flv4j.codecs.video.ChromaSubsampling;
import co.casterlabs.flv4j.codecs.video.ColorPrimaries;
import co.casterlabs.flv4j.codecs.video.MatrixCoefficients;
import co.casterlabs.flv4j.codecs.video.TransferCharacteristics;
import co.casterlabs.flv4j.codecs.video.hvc1.HEVCDecoderConfigurationRecord;
import co.casterlabs.flv4j.codecs.video.hvc1.HEVCExVideoData;
import co.casterlabs.flv4j.codecs.video.hvc1.HEVCNalu;
import co.casterlabs.flv4j.codecs.video.hvc1.HEVCNaluType;
import co.casterlabs.flv4j.codecs.video.hvc1.HEVCNalus;
import co.casterlabs.flv4j.codecs.video.hvc1.HEVCPPS;
import co.casterlabs.flv4j.codecs.video.hvc1.HEVCPacketType;
import co.casterlabs.flv4j.codecs.video.hvc1.HEVCSPS;
import co.casterlabs.flv4j.codecs.video.hvc1.HEVCVideoData;
import co.casterlabs.flv4j.flv.FLVFileHeader;
import co.casterlabs.flv4j.flv.muxing.NonSeekableFLVDemuxer;
import co.casterlabs.flv4j.flv.tags.FLVTag;
import co.casterlabs.flv4j.flv.tags.video.FLVStandardVideoTagData;
import co.casterlabs.flv4j.flv.tags.video.FLVVideoCodec;
import co.casterlabs.flv4j.flv.tags.video.ex.FLVExVideoTagData;
import co.casterlabs.flv4j.flv.tags.video.ex.FLVExVideoTrack;
import co.casterlabs.flv4j.test.Media;

public class HEVC {

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
    public void nonstandard_HEVC() throws IOException {
        videoTest("nonstandard/video/hevc", FLVVideoCodec.NS_HEVC);
    }

    @Test
    public void nonstandard_HEVC_structure() throws IOException {
        boolean[] foundSeqHeader = {
                false
        };
        boolean[] foundNalu = {
                false
        };

        new NonSeekableFLVDemuxer() {
            @Override
            protected void onHeader(FLVFileHeader header) {}

            @Override
            protected void onTag(long previousTagSize, FLVTag tag) {
                if (tag.data() instanceof FLVStandardVideoTagData data && data.codec() == FLVVideoCodec.NS_HEVC) {
                    if (data.data() instanceof HEVCVideoData hevc) {
                        if (hevc.type() == HEVCPacketType.SEQUENCE_HEADER && hevc.packetData() instanceof HEVCDecoderConfigurationRecord record) {
                            foundSeqHeader[0] = true;

                            // ffprobe ground truth (enhanced source encode): hevc Main, level 2.0
                            // (level_idc 60), yuv420p.
                            assertEquals(1, record.configurationVersion());
                            assertEquals(0, record.generalProfileSpace());
                            assertEquals(1, record.generalProfileIdc()); // Main
                            assertEquals(60, record.generalLevelIdc()); // level 2.0 (level_idc 60)
                            assertEquals(1, record.chromaFormat()); // 4:2:0
                            assertEquals(0, record.bitDepthLumaMinus8());
                            assertEquals(0, record.bitDepthChromaMinus8());
                            assertEquals(3, record.lengthSizeMinusOne());
                            assertEquals(4, record.numOfArrays()); // VPS, SPS, PPS, SEI

                            // The 4 NAL arrays: [type][count][nalus] (hvcC ground truth).
                            assertEquals(4, record.naluArrays().length);
                            assertEquals(HEVCNaluType.VPS, record.naluArrayTypes()[0]);
                            assertEquals(HEVCNaluType.SPS, record.naluArrayTypes()[1]);
                            assertEquals(HEVCNaluType.PPS, record.naluArrayTypes()[2]);
                            assertEquals(HEVCNaluType.PREFIX_SEI, record.naluArrayTypes()[3]);
                            for (HEVCNalu[] nalus : record.naluArrays()) {
                                assertEquals(1, nalus.length);
//                                assertEquals(1, nalus.nalus().length);
                            }
                            assertEquals(24, record.naluArrays()[0][0].view().length()); // VPS
                            assertEquals(43, record.naluArrays()[1][0].view().length()); // SPS
                            assertEquals(7, record.naluArrays()[2][0].view().length()); // PPS
                            assertEquals(2363, record.naluArrays()[3][0].view().length()); // PREFIX_SEI
                        } else if (hevc.type() == HEVCPacketType.NALU) {
                            foundNalu[0] = true;
                        }
                    }
                }
            }

            @Override
            protected void onEnd() {}
        }.start(Media.stream("nonstandard/video/hevc"));

        assertTrue(foundSeqHeader[0], "Did not find a HEVC sequence header");
        assertTrue(foundNalu[0], "Did not find any HEVC NALU frames");
    }

    @Test
    public void nonstandard_HEVC_coded_frames() throws IOException {
        List<HEVCNalus> naluFrames = new ArrayList<>();

        new NonSeekableFLVDemuxer() {
            @Override
            protected void onHeader(FLVFileHeader header) {}

            @Override
            protected void onTag(long previousTagSize, FLVTag tag) {
                if (tag.data() instanceof FLVStandardVideoTagData data && data.codec() == FLVVideoCodec.NS_HEVC) {
                    if (data.data() instanceof HEVCVideoData hevc && hevc.type() == HEVCPacketType.NALU
                        && hevc.packetData() instanceof HEVCNalus list) {
                        naluFrames.add(list);
                    }
                }
            }

            @Override
            protected void onEnd() {}
        }.start(Media.stream("nonstandard/video/hevc"));

        assertEquals(240, naluFrames.size()); // 10s @ 24fps, one NALU per frame

        // First frame: the IDR NALU.
        assertEquals(1, naluFrames.get(0).numberOfNalus());
        HEVCNalu first = naluFrames.get(0).nalus()[0];
        assertEquals(HEVCNaluType.IDR_N_LP, first.type());
        assertEquals(53, first.view().length());

        // Second frame: a non-IDR trail.
        assertEquals(1, naluFrames.get(1).numberOfNalus());
        assertEquals(HEVCNaluType.TRAIL_R, naluFrames.get(1).nalus()[0].type());
    }

    @Test
    public void veovera_HEVC_structure() throws IOException {
        boolean[] foundSeqHeader = {
                false
        };

        new NonSeekableFLVDemuxer() {
            @Override
            protected void onHeader(FLVFileHeader header) {}

            @Override
            protected void onTag(long previousTagSize, FLVTag tag) {
                if (tag.data() instanceof FLVExVideoTagData ex) {
                    for (FLVExVideoTrack track : ex.tracks()) {
                        if (track.codec().string().equals("hvc1") && track.data() instanceof HEVCExVideoData h
                            && h.isSequenceHeader() && h.packetData() instanceof HEVCDecoderConfigurationRecord record) {
                            foundSeqHeader[0] = true;

                            // ffprobe ground truth (veovera/video/hvc1.flv): hevc Main,
                            // level 2.0 (level_idc 60), 320x180, yuv420p, 24/1.
                            assertEquals(1, record.configurationVersion());
                            assertEquals(0, record.generalProfileSpace());
                            assertEquals(1, record.generalProfileIdc()); // Main
                            assertEquals(60, record.generalLevelIdc()); // level 2.0 (level_idc 60)
                            assertEquals(1, record.chromaFormat()); // 4:2:0
                            assertEquals(0, record.bitDepthLumaMinus8());
                            assertEquals(0, record.bitDepthChromaMinus8());
                            assertEquals(3, record.lengthSizeMinusOne());
                            assertEquals(4, record.numOfArrays()); // VPS, SPS, PPS, SEI

                            // The 4 NAL arrays: [type][count][nalus] (hvcC ground truth).
                            assertEquals(HEVCNaluType.VPS, record.naluArrayTypes()[0]);
                            assertEquals(HEVCNaluType.SPS, record.naluArrayTypes()[1]);
                            assertEquals(HEVCNaluType.PPS, record.naluArrayTypes()[2]);
                            assertEquals(HEVCNaluType.PREFIX_SEI, record.naluArrayTypes()[3]);
                            assertEquals(24, record.naluArrays()[0][0].view().length()); // VPS
                            assertEquals(43, record.naluArrays()[1][0].view().length()); // SPS
                            assertEquals(7, record.naluArrays()[2][0].view().length()); // PPS
                            assertEquals(2370, record.naluArrays()[3][0].view().length()); // PREFIX_SEI
                        }
                    }
                }
            }

            @Override
            protected void onEnd() {}
        }.start(Media.stream("veovera/video/hvc1"));

        assertTrue(foundSeqHeader[0], "Did not find a hvc1 sequence header");
    }

    @Test
    public void veovera_HEVC_coded_frames() throws IOException {
        List<HEVCNalus> naluFrames = new ArrayList<>();
        int[] keyframes = {
                0
        };

        new NonSeekableFLVDemuxer() {
            @Override
            protected void onHeader(FLVFileHeader header) {}

            @Override
            protected void onTag(long previousTagSize, FLVTag tag) {
                if (tag.data() instanceof FLVExVideoTagData ex) {
                    for (FLVExVideoTrack track : ex.tracks()) {
                        if (track.codec().string().equals("hvc1") && track.data() instanceof HEVCExVideoData h
                            && h.packetData() instanceof HEVCNalus list) {
                            naluFrames.add(list);
                            if (ex.frameType().id == 1) {
                                keyframes[0]++;
                            }
                        }
                    }
                }
            }

            @Override
            protected void onEnd() {}
        }.start(Media.stream("veovera/video/hvc1"));

        // ffprobe ground truth (veovera/video/hvc1.flv): 240 packets (2 I + 72 P + 166
        // B),
        // 1 keyframe (K__) coded tag, one NALU per frame. Both the CODED_FRAMES type-1
        // tags (explicit 24-bit CTS) and the CODED_FRAMES_X type-3 tags (implicit zero
        // CTS) parse to the typed NALU list.
        assertEquals(240, naluFrames.size());
        assertEquals(1, keyframes[0]);
        for (HEVCNalus list : naluFrames) {
            assertEquals(1, list.numberOfNalus());
        }

        // First frame: the IDR NALU.
        HEVCNalu first = naluFrames.get(0).nalus()[0];
        assertEquals(HEVCNaluType.IDR_N_LP, first.type());
        assertEquals(53, first.view().length());

        // Second frame: a non-IDR trail.
        assertEquals(HEVCNaluType.TRAIL_R, naluFrames.get(1).nalus()[0].type());
    }

    @Test
    public void nonstandard_HEVC_sps() throws IOException {
        HEVCSPS[] foundSps = {
                null
        };

        new NonSeekableFLVDemuxer() {
            @Override
            protected void onHeader(FLVFileHeader header) {}

            @Override
            protected void onTag(long previousTagSize, FLVTag tag) {
                if (tag.data() instanceof FLVStandardVideoTagData data && data.codec() == FLVVideoCodec.NS_HEVC) {
                    if (data.data() instanceof HEVCVideoData hevc && hevc.type() == HEVCPacketType.SEQUENCE_HEADER
                        && hevc.packetData() instanceof HEVCDecoderConfigurationRecord record) {
                        foundSps[0] = HEVCSPS.from(record.nalus(HEVCNaluType.SPS)[0]);
                    }
                }
            }

            @Override
            protected void onEnd() {}
        }.start(Media.stream("nonstandard/video/hevc"));

        assertNotNull(foundSps[0], "Did not find a HEVC SPS");
        HEVCSPS sps = foundSps[0];

        // ffprobe ground truth (enhanced source encode): hevc Main(1), level 2.0
        // (level_idc 60),
        // 320x180, yuv420p, 8-bit, 24fps. The coded size is 320x184 -- the conformance
        // window (bottom=2, chroma-sampled) yields the 320x180 display size.
        assertEquals(0, sps.vpsId());
        assertEquals(0, sps.spsId());
        assertEquals(1, sps.profileIdc()); // Main
        assertEquals(60, sps.levelIdc()); // level 2.0 (level_idc 60)
        assertEquals(1, sps.chromaFormatIdc()); // 4:2:0
        assertEquals(ChromaSubsampling.CS_420, sps.chromaSubsampling());
        assertEquals(0, sps.bitDepthLumaMinus8());
        assertEquals(0, sps.bitDepthChromaMinus8());
        assertEquals(ColorPrimaries.RESERVED, sps.colourPrimaries());
        assertEquals(TransferCharacteristics.RESERVED, sps.transferCharacteristics());
        assertEquals(MatrixCoefficients.IDENTITY, sps.matrixCoefficients());
        assertEquals(320, sps.picWidthInLumaSamples());
        assertEquals(184, sps.picHeightInLumaSamples());
        assertEquals(320, sps.displayWidth());
        assertEquals(180, sps.displayHeight());
        assertEquals(2, sps.conformanceWindowBottomOffset());
        assertEquals(1, sps.numUnitsInTick());
        assertEquals(24, sps.timeScale());
        assertEquals(24.0, sps.fps(), 0.001);
    }

    @Test
    public void nonstandard_HEVC_pps() throws IOException {
        HEVCPPS[] foundPps = {
                null
        };

        new NonSeekableFLVDemuxer() {
            @Override
            protected void onHeader(FLVFileHeader header) {}

            @Override
            protected void onTag(long previousTagSize, FLVTag tag) {
                if (tag.data() instanceof FLVStandardVideoTagData data && data.codec() == FLVVideoCodec.NS_HEVC) {
                    if (data.data() instanceof HEVCVideoData hevc && hevc.type() == HEVCPacketType.SEQUENCE_HEADER
                        && hevc.packetData() instanceof HEVCDecoderConfigurationRecord record) {
                        foundPps[0] = HEVCPPS.from(record.nalus(HEVCNaluType.PPS)[0]);
                    }
                }
            }

            @Override
            protected void onEnd() {}
        }.start(Media.stream("nonstandard/video/hevc"));

        assertNotNull(foundPps[0], "Did not find a HEVC PPS");
        // ffprobe does not emit PPS fields, so the bar is a clean parse of the
        // stream-info fields against plausible values.
        assertEquals(0, foundPps[0].ppsId());
        assertEquals(0, foundPps[0].spsId());
        assertTrue(foundPps[0].signDataHidingEnabled());
        assertEquals(0, foundPps[0].initQpMinus26()); // init_qp = 26
        assertEquals(0, foundPps[0].ppsCbQpOffset());
        assertEquals(0, foundPps[0].ppsCrQpOffset());
    }

    @Test
    public void nonstandard_HEVC_signed_composition_time_offset() {
        // Packet type 1 (NALU), CTS = -2 (0xFFFFFE), with one 2-byte NALU payload.
        byte[] data = {
                0x01,
                (byte) 0xFF,
                (byte) 0xFF,
                (byte) 0xFE,
                0x00,
                0x00,
                0x00,
                0x02,
                0x26,
                0x01
        };

        HEVCVideoData parsed = new HEVCVideoData(new ASByteView(data));
        assertEquals(-2, parsed.compositionTimeOffset());
    }

}
