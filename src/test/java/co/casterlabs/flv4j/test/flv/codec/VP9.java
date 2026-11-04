package co.casterlabs.flv4j.test.flv.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import co.casterlabs.flv4j.codecs.video.ChromaSubsampling;
import co.casterlabs.flv4j.codecs.video.ColorPrimaries;
import co.casterlabs.flv4j.codecs.video.MatrixCoefficients;
import co.casterlabs.flv4j.codecs.video.TransferCharacteristics;
import co.casterlabs.flv4j.codecs.video.vpx.VPCodecConfigurationRecord;
import co.casterlabs.flv4j.codecs.video.vpx.VPCodedData;
import co.casterlabs.flv4j.codecs.video.vpx.VPExVideoData;
import co.casterlabs.flv4j.flv.FLVFileHeader;
import co.casterlabs.flv4j.flv.muxing.NonSeekableFLVDemuxer;
import co.casterlabs.flv4j.flv.tags.FLVTag;
import co.casterlabs.flv4j.flv.tags.video.ex.FLVExVideoTagData;
import co.casterlabs.flv4j.flv.tags.video.ex.FLVExVideoTrack;
import co.casterlabs.flv4j.test.Media;

public class VP9 {

    @Test
    public void veovera_VP9_structure() throws IOException {
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
                        if (track.codec().string().equals("vp09") && track.data() instanceof VPExVideoData vp
                            && vp.isSequenceHeader() && vp.packetData() instanceof VPCodecConfigurationRecord record) {
                            foundSeqHeader[0] = true;

                            // ffprobe ground truth (vp09.flv): vp9 Profile 0, 320x180, yuv420p,
                            // 24/1, color_range tv. The level (11) is not in ffprobe's output, but
                            // matches FFmpeg's get_vp9_level() for 320x180@24fps.
                            assertEquals(1, record.configurationVersion());
                            assertEquals(0, record.reserved());
                            assertEquals(0, record.profile()); // Profile 0
                            assertEquals(11, record.level());
                            assertEquals(8, record.bitDepth());
                            assertEquals(ChromaSubsampling.CS_420, record.chromaSubsampling()); // 4:2:0 (yuv420p)
                            assertEquals(false, record.videoFullRangeFlag()); // tv range
                            assertEquals(ColorPrimaries.UNSPECIFIED, record.colourPrimaries()); // BT.709
                            assertEquals(TransferCharacteristics.UNSPECIFIED, record.transferCharacteristics());
                            assertEquals(MatrixCoefficients.UNSPECIFIED, record.matrixCoefficients());
                            assertEquals(0, record.codecInitialisationDataSize());
                        }
                    }
                }
            }

            @Override
            protected void onEnd() {}
        }.start(Media.stream("veovera/video/vp09"));

        assertTrue(foundSeqHeader[0], "Did not find a VP9 sequence header");
    }

    @Test
    public void veovera_VP9_coded_frames() throws IOException {
        int[] frames = {
                0
        };
        int[] keyframes = {
                0
        };
        int[] firstKeySize = {
                0
        };

        new NonSeekableFLVDemuxer() {
            @Override
            protected void onHeader(FLVFileHeader header) {}

            @Override
            protected void onTag(long previousTagSize, FLVTag tag) {
                if (tag.data() instanceof FLVExVideoTagData ex) {
                    for (FLVExVideoTrack track : ex.tracks()) {
                        if (track.codec().string().equals("vp09") && track.data() instanceof VPExVideoData vp
                            && vp.packetData() instanceof VPCodedData coded) {
                            if (ex.frameType().id == 1) {
                                if (keyframes[0] == 0) {
                                    firstKeySize[0] = coded.view().length();
                                }
                                keyframes[0]++;
                            }
                            frames[0]++;
                        }
                    }
                }
            }

            @Override
            protected void onEnd() {}
        }.start(Media.stream("veovera/video/vp09"));

        // ffprobe ground truth (vp09.flv): 240 packets, 2 K__ + 238 ___. The VP9
        // frames have no composition time offset (see the eFLV spec).
        assertEquals(240, frames[0]);
        assertEquals(2, keyframes[0]);
        assertEquals(43, firstKeySize[0]);
    }

}
