package co.casterlabs.flv4j.test.flv;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import co.casterlabs.flv4j.flv.FLVFileHeader;
import co.casterlabs.flv4j.flv.muxing.NonSeekableFLVDemuxer;
import co.casterlabs.flv4j.flv.tags.FLVTag;
import co.casterlabs.flv4j.flv.tags.video.FLVStandardVideoTagData;
import co.casterlabs.flv4j.flv.tags.video.FLVVideoCodec;

public class FLVVideo {

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
        }.start(_Media.stream(name));

        assertTrue(foundTag[0], "Did not find any video tags in " + name);
    }

    @Test
    public void standard_H264() throws IOException {
        videoTest("standard/video/h264", FLVVideoCodec.H264);
    }

    @Test
    public void standard_On2Vp6() throws IOException {
        videoTest("standard/video/on2_vp6", FLVVideoCodec.ON2_VP6);
    }

    @Test
    public void standard_Screen() throws IOException {
        videoTest("standard/video/screen", FLVVideoCodec.SCREEN);
    }

    @Test
    public void standard_Screen2() throws IOException {
        videoTest("standard/video/screen2", FLVVideoCodec.SCREEN_2);
    }

    @Test
    public void standard_SorensonH263() throws IOException {
        videoTest("standard/video/sorenson_h263", FLVVideoCodec.SORENSON_H263);
    }

    @Test
    public void nonstandard_HEVC() throws IOException {
        videoTest("nonstandard/video/hevc", FLVVideoCodec.NS_HEVC);
    }

    @Test
    public void nonstandard_mpeg4() throws IOException {
        videoTest("nonstandard/video/mpeg4", FLVVideoCodec.NS_MPEG4);
    }

    @Test
    public void nonstandard_RealH263() throws IOException {
        videoTest("nonstandard/video/realh263", FLVVideoCodec.NS_REALH263);
    }

}
