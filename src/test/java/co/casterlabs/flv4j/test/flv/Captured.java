package co.casterlabs.flv4j.test.flv;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import co.casterlabs.flv4j.codecs.video.av01.AV1CodecConfigurationRecord;
import co.casterlabs.flv4j.codecs.video.av01.AV1ExVideoData;
import co.casterlabs.flv4j.codecs.video.av01.AV1SequenceHeader;
import co.casterlabs.flv4j.codecs.video.avc1.AVCDecoderConfigurationRecord;
import co.casterlabs.flv4j.codecs.video.avc1.AVCNalu;
import co.casterlabs.flv4j.codecs.video.avc1.AVCPPS;
import co.casterlabs.flv4j.codecs.video.avc1.AVCSPS;
import co.casterlabs.flv4j.codecs.video.avc1.AVCVideoData;
import co.casterlabs.flv4j.codecs.video.hvc1.HEVCDecoderConfigurationRecord;
import co.casterlabs.flv4j.codecs.video.hvc1.HEVCExVideoData;
import co.casterlabs.flv4j.codecs.video.hvc1.HEVCNalu;
import co.casterlabs.flv4j.codecs.video.hvc1.HEVCNaluType;
import co.casterlabs.flv4j.codecs.video.hvc1.HEVCPPS;
import co.casterlabs.flv4j.codecs.video.hvc1.HEVCSPS;
import co.casterlabs.flv4j.flv.FLVFileHeader;
import co.casterlabs.flv4j.flv.muxing.NonSeekableFLVDemuxer;
import co.casterlabs.flv4j.flv.tags.FLVTag;
import co.casterlabs.flv4j.flv.tags.video.FLVStandardVideoTagData;
import co.casterlabs.flv4j.flv.tags.video.ex.FLVExVideoTagData;
import co.casterlabs.flv4j.flv.tags.video.ex.FLVExVideoTrack;
import co.casterlabs.flv4j.test.Media;

public class Captured {

    @Test
    public void avc1_aac() throws IOException {
        new NonSeekableFLVDemuxer() {
            @Override
            protected void onHeader(FLVFileHeader header) {}

            @Override
            protected void onTag(long previousTagSize, FLVTag tag) {
                tag.toString(); // Just to make sure it doesn't throw an exception.
                if (tag.data() instanceof FLVStandardVideoTagData data) {
                    if (data.data() instanceof AVCVideoData avc) {
                        if (avc.packetData() instanceof AVCDecoderConfigurationRecord dcr) {
                            dcr.toString();
                            for (AVCNalu nalu : dcr.sps()) {
                                AVCSPS sps = AVCSPS.from(nalu);
                                sps.toString();
                            }
                            for (AVCNalu nalu : dcr.pps()) {
                                AVCPPS pps = AVCPPS.from(nalu);
                                pps.toString();
                            }
                        }
                    }
                }
            }

            @Override
            protected void onEnd() {}
        }.start(Media.stream("captured/avc1_aac"));
    }

    @Test
    public void hevc_aac() throws IOException {
        new NonSeekableFLVDemuxer() {
            @Override
            protected void onHeader(FLVFileHeader header) {}

            @Override
            protected void onTag(long previousTagSize, FLVTag tag) {
                tag.toString(); // Just to make sure it doesn't throw an exception.
                if (tag.data() instanceof FLVExVideoTagData data) {
                    for (FLVExVideoTrack track : data.tracks()) {
                        if (track.data() instanceof HEVCExVideoData av1) {
                            if (av1.packetData() instanceof HEVCDecoderConfigurationRecord dcr) {
                                dcr.toString();
                                HEVCNalu[] spsNalus = dcr.nalus(HEVCNaluType.SPS);
                                for (HEVCNalu nalu : spsNalus) {
                                    HEVCSPS sps = HEVCSPS.from(nalu);
                                    sps.toString();
                                }

                                HEVCNalu[] ppsNalus = dcr.nalus(HEVCNaluType.PPS);
                                for (HEVCNalu nalu : ppsNalus) {
                                    HEVCPPS pps = HEVCPPS.from(nalu);
                                    pps.toString();
                                }
                            }
                        }
                    }
                }
            }

            @Override
            protected void onEnd() {}
        }.start(Media.stream("captured/hevc_aac"));
    }

    @Test
    public void hevc_aac_hdr() throws IOException {
        new NonSeekableFLVDemuxer() {
            @Override
            protected void onHeader(FLVFileHeader header) {}

            @Override
            protected void onTag(long previousTagSize, FLVTag tag) {
                tag.toString(); // Just to make sure it doesn't throw an exception.
                if (tag.data() instanceof FLVExVideoTagData data) {
                    for (FLVExVideoTrack track : data.tracks()) {
                        if (track.data() instanceof HEVCExVideoData av1) {
                            if (av1.packetData() instanceof HEVCDecoderConfigurationRecord dcr) {
                                dcr.toString();
                                HEVCNalu[] spsNalus = dcr.nalus(HEVCNaluType.SPS);
                                for (HEVCNalu nalu : spsNalus) {
                                    HEVCSPS sps = HEVCSPS.from(nalu);
                                    sps.toString();
                                }

                                HEVCNalu[] ppsNalus = dcr.nalus(HEVCNaluType.PPS);
                                for (HEVCNalu nalu : ppsNalus) {
                                    HEVCPPS pps = HEVCPPS.from(nalu);
                                    pps.toString();
                                }
                            }
                        }
                    }
                }
            }

            @Override
            protected void onEnd() {}
        }.start(Media.stream("captured/hevc_aac_hdr"));
    }

    @Test
    public void av01_aac() throws IOException {
        new NonSeekableFLVDemuxer() {
            @Override
            protected void onHeader(FLVFileHeader header) {}

            @Override
            protected void onTag(long previousTagSize, FLVTag tag) {
                tag.toString(); // Just to make sure it doesn't throw an exception.
                if (tag.data() instanceof FLVExVideoTagData data) {
                    for (FLVExVideoTrack track : data.tracks()) {
                        if (track.data() instanceof AV1ExVideoData av1) {
                            if (av1.packetData() instanceof AV1CodecConfigurationRecord ccr) {
                                ccr.toString();
                                for (AV1SequenceHeader header : ccr.sequenceHeaders()) {
                                    header.toString();
                                }
                            }
                        }
                    }
                }
            }

            @Override
            protected void onEnd() {}
        }.start(Media.stream("captured/av01_aac"));
    }

}
