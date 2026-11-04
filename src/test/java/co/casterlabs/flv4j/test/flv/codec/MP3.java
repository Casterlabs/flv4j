package co.casterlabs.flv4j.test.flv.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import co.casterlabs.flv4j.codecs.audio.mp3.MP3AudioData;
import co.casterlabs.flv4j.codecs.audio.mp3.MP3Frame;
import co.casterlabs.flv4j.codecs.audio.mp3.MP3FrameHeader;
import co.casterlabs.flv4j.flv.FLVFileHeader;
import co.casterlabs.flv4j.flv.muxing.NonSeekableFLVDemuxer;
import co.casterlabs.flv4j.flv.tags.FLVTag;
import co.casterlabs.flv4j.flv.tags.audio.FLVAudioFormat;
import co.casterlabs.flv4j.flv.tags.audio.FLVStandardAudioTagData;
import co.casterlabs.flv4j.test.Media;

public class MP3 {

    private void audioTest(String name, FLVAudioFormat expectedCodec) throws IOException {
        boolean[] foundTag = {
                false
        }; // pointer hax

        new NonSeekableFLVDemuxer() {
            @Override
            protected void onHeader(FLVFileHeader header) {}

            @Override
            protected void onTag(long previousTagSize, FLVTag tag) {
                if (tag.data() instanceof FLVStandardAudioTagData data) {
                    foundTag[0] = true;
                    assertTrue(data.format() == expectedCodec, "Failed to find expected codec: " + expectedCodec + " in " + name + ". got: " + data.format());
                }
            }

            @Override
            protected void onEnd() {}
        }.start(Media.stream(name));

        assertTrue(foundTag[0], "Did not find any audio tags in " + name);
    }

    @Test
    public void standard_mp3() throws IOException {
        audioTest("standard/audio/mp3", FLVAudioFormat.MP3);
    }

    @Test
    public void standard_MP3_frame_header() throws IOException {
        MP3FrameHeader[] firstHeader = {
                null
        };
        List<MP3Frame> frames = new ArrayList<>();

        new NonSeekableFLVDemuxer() {
            @Override
            protected void onHeader(FLVFileHeader header) {}

            @Override
            protected void onTag(long previousTagSize, FLVTag tag) {
                if (tag.data() instanceof FLVStandardAudioTagData data && data.data() instanceof MP3AudioData mp3) {
                    for (MP3Frame frame : mp3.frames()) {
                        if (firstHeader[0] == null) {
                            firstHeader[0] = frame.header();
                        }
                        frames.add(frame);
                    }
                }
            }

            @Override
            protected void onEnd() {}
        }.start(Media.stream("standard/audio/mp3"));

        assertNotNull(firstHeader[0], "Did not find an MP3 frame header");
        MP3FrameHeader header = firstHeader[0];

        // ffprobe ground truth (mp3.flv): mp3, 44100 Hz, 2 channels, 128000 bps.
        assertEquals(true, header.sync());
        assertEquals(3, header.version()); // MPEG-1
        assertEquals(1, header.layer()); // Layer III
        assertEquals(false, header.hasCRC());
        assertEquals(9, header.bitrateIndex());
        assertEquals(128000, header.bitrate());
        assertEquals(0, header.sampleRateIndex());
        assertEquals(44100, header.sampleRate());
        assertEquals(false, header.padding()); // the first frame is unpadded
        assertEquals(false, header.privateBit());
        assertEquals(1, header.channelMode()); // joint stereo (2 channels)
        assertEquals(0, header.modeExtension());
        assertEquals(false, header.copyright());
        assertEquals(true, header.original());
        assertEquals(0, header.emphasis());
        assertEquals(417, header.frameSize()); // 144 * 128000 / 44100, unpadded

        // Only the padding bit varies between frames in this fixture.
        assertEquals(383, frames.size());
        long padded = frames.stream().filter(f -> f.header().padding()).count();
        assertEquals(367, padded);
    }

}
