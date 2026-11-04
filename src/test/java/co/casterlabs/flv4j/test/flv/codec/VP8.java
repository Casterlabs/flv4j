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

public class VP8 {

    @Test
    public void veovera_VP8_structure() throws IOException {
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
                        if (track.codec().string().equals("vp08") && track.data() instanceof VPExVideoData vp
                            && vp.isSequenceHeader() && vp.packetData() instanceof VPCodecConfigurationRecord record) {
                            foundSeqHeader[0] = true;

                            // Cross-checked against the source WebM's ffprobe output (vp8, profile 0,
                            // 320x180, yuv420p, color_range tv, 24/1) -- ffprobe 7.1.5 cannot parse
                            // enhanced vp08 FLV (no vp08 case in flvdec). The level (1) and the BT.709
                            // colour triple are synthesized; see the fixture's provenance in sources.txt.
                            assertEquals(1, record.configurationVersion());
                            assertEquals(0, record.profile()); // profile 0
                            assertEquals(1, record.level()); // synthesized
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
        }.start(Media.stream("veovera/video/vp08"));

        assertTrue(foundSeqHeader[0], "Did not find a VP8 sequence header");
    }

    @Test
    public void veovera_VP8_coded_frames() throws IOException {
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
                        if (track.codec().string().equals("vp08") && track.data() instanceof VPExVideoData vp
                            && vp.packetData() instanceof VPCodedData) {
                            if (ex.frameType().id == 1) {
                                keyframes[0]++;
                            }
                            frames[0]++;
                        }
                    }
                }
            }

            @Override
            protected void onEnd() {}
        }.start(Media.stream("veovera/video/vp08"));

        // Cross-checked against the source WebM's ffprobe output: 240 frames,
        // 3 K__ + 237 ___.
        assertEquals(240, frames[0]);
        assertEquals(3, keyframes[0]);
    }

}
