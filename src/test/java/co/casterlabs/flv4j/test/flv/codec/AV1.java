package co.casterlabs.flv4j.test.flv.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import co.casterlabs.flv4j.codecs.video.ChromaSubsampling;
import co.casterlabs.flv4j.codecs.video.ColorPrimaries;
import co.casterlabs.flv4j.codecs.video.MatrixCoefficients;
import co.casterlabs.flv4j.codecs.video.TransferCharacteristics;
import co.casterlabs.flv4j.codecs.video.av01.AV1CodecConfigurationRecord;
import co.casterlabs.flv4j.codecs.video.av01.AV1ExVideoData;
import co.casterlabs.flv4j.codecs.video.av01.AV1OBU;
import co.casterlabs.flv4j.codecs.video.av01.AV1OBUType;
import co.casterlabs.flv4j.codecs.video.av01.AV1OBUs;
import co.casterlabs.flv4j.codecs.video.av01.AV1SequenceHeader;
import co.casterlabs.flv4j.flv.FLVFileHeader;
import co.casterlabs.flv4j.flv.muxing.NonSeekableFLVDemuxer;
import co.casterlabs.flv4j.flv.tags.FLVTag;
import co.casterlabs.flv4j.flv.tags.video.ex.FLVExVideoTagData;
import co.casterlabs.flv4j.flv.tags.video.ex.FLVExVideoTrack;
import co.casterlabs.flv4j.test.Media;

public class AV1 {

    @Test
    public void veovera_AV1_structure() throws IOException {
        boolean[] foundSeqHeader = {
                false
        };
        AV1OBU[][] firstKeyObus = {
                null
        };

        new NonSeekableFLVDemuxer() {
            @Override
            protected void onHeader(FLVFileHeader header) {}

            @Override
            protected void onTag(long previousTagSize, FLVTag tag) {
                if (tag.data() instanceof FLVExVideoTagData ex) {
                    for (FLVExVideoTrack track : ex.tracks()) {
                        if (track.codec().string().equals("av01") && track.data() instanceof AV1ExVideoData av1) {
                            if (av1.isSequenceHeader() && av1.packetData() instanceof AV1CodecConfigurationRecord record) {
                                foundSeqHeader[0] = true;

                                // ffprobe ground truth (veovera/video/av01.flv): av1 Main,
                                // 320x180, yuv420p. The 4-byte reduced av1c record
                                // (81 1f 0c 00), per ff_isom_write_av1c.
                                assertEquals(1, record.marker());
                                assertEquals(1, record.version());
                                assertEquals(0, record.profile()); // Main
                                assertEquals(31, record.seqLevelIdx());
                                assertEquals(0, record.tier());
                                assertEquals(8, record.bitDepth()); // 8-bit
                                assertEquals(false, record.monochrome());
                                assertEquals(1, record.chromaSubsamplingX()); // 4:2:0 (yuv420p)
                                assertEquals(1, record.chromaSubsamplingY());
                                assertEquals(0, record.chromaSamplePosition());
                                assertEquals(0, record.reserved3Bits());
                                assertEquals(false, record.initialPresentationDelayPresent());
                                assertEquals(-1, record.initialPresentationDelayMinusOne());
                                assertEquals(0, record.reserved4Bits());
                                assertEquals(0, record.sequenceHeaders().length);
                                assertEquals(0, record.metadata().length);
                            } else if (av1.packetData() instanceof AV1OBUs obus
                                && ex.frameType().id == 1 && firstKeyObus[0] == null) {
                                    firstKeyObus[0] = obus.obus();
                                }
                        }
                    }
                }
            }

            @Override
            protected void onEnd() {}
        }.start(Media.stream("veovera/video/av01"));

        assertTrue(foundSeqHeader[0], "Did not find an av01 sequence header");
        assertNotNull(firstKeyObus[0], "Did not find the first av01 keyframe");

        // The first keyframe: a TEMPORAL_DELIMITER, the SEQUENCE_HEADER, and the
        // FRAME OBU (2 + 16 + 51 = 69 bytes, clean end).
        assertEquals(3, firstKeyObus[0].length);
        assertEquals(AV1OBUType.TEMPORAL_DELIMITER, firstKeyObus[0][0].type());
        assertEquals(0, firstKeyObus[0][0].dataLength());
        assertEquals(AV1OBUType.SEQUENCE_HEADER, firstKeyObus[0][1].type());
        assertEquals(14, firstKeyObus[0][1].dataLength());
        assertEquals(AV1OBUType.FRAME, firstKeyObus[0][2].type());
        assertEquals(49, firstKeyObus[0][2].dataLength());

        AV1SequenceHeader seq = AV1SequenceHeader.from(firstKeyObus[0][1]);
        assertEquals(0, seq.seqProfile());
        assertEquals(31, seq.seqLevelIdx());
        assertEquals(8, seq.bitDepth());
        assertEquals(false, seq.monochrome());
        assertEquals(false, seq.fullRange());
        assertEquals(ChromaSubsampling.CS_420, seq.chromaSubsampling());
        assertEquals(MatrixCoefficients.UNSPECIFIED, seq.matrixCoefficients());
        assertEquals(ColorPrimaries.UNSPECIFIED, seq.colourPrimaries());
        assertEquals(TransferCharacteristics.UNSPECIFIED, seq.transferCharacteristics());
        assertEquals(320, seq.maxFrameWidth());
        assertEquals(180, seq.maxFrameHeight());
    }

    @Test
    public void veovera_AV1_coded_frames() throws IOException {
        int[] frames = {
                0
        };
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
                        if (track.codec().string().equals("av01") && track.data() instanceof AV1ExVideoData av1
                            && av1.packetData() instanceof AV1OBUs) {
                            frames[0]++;
                            if (ex.frameType().id == 1) {
                                keyframes[0]++;
                            }
                        }
                    }
                }
            }

            @Override
            protected void onEnd() {}
        }.start(Media.stream("veovera/video/av01"));

        // ffprobe ground truth (veovera/video/av01.flv): 240 frames (1 keyframe,
        // 239 inter), 320x180, 24fps (librav1e). Every coded frame's OBU stream
        // walks to a clean end (the AV1OBUs parse validates it).
        assertEquals(240, frames[0]);
        assertEquals(1, keyframes[0]);
    }

}
