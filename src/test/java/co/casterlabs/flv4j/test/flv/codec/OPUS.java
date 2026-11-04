package co.casterlabs.flv4j.test.flv.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import co.casterlabs.flv4j.codecs.audio.opus.OpusCodedData;
import co.casterlabs.flv4j.codecs.audio.opus.OpusExAudioData;
import co.casterlabs.flv4j.codecs.audio.opus.OpusHead;
import co.casterlabs.flv4j.flv.FLVFileHeader;
import co.casterlabs.flv4j.flv.muxing.NonSeekableFLVDemuxer;
import co.casterlabs.flv4j.flv.tags.FLVTag;
import co.casterlabs.flv4j.flv.tags.audio.ex.FLVExAudioTagData;
import co.casterlabs.flv4j.flv.tags.audio.ex.FLVExAudioTrack;
import co.casterlabs.flv4j.test.Media;

public class OPUS {

    private void audioTest(String name, String fourcc) throws IOException {
        boolean[] foundTag = {
                false
        }; // pointer hax

        new NonSeekableFLVDemuxer() {
            @Override
            protected void onHeader(FLVFileHeader header) {}

            @Override
            protected void onTag(long previousTagSize, FLVTag tag) {
                if (tag.data() instanceof FLVExAudioTagData data) {
                    for (FLVExAudioTrack track : data.tracks()) {
                        foundTag[0] = true;
                        assertTrue(track.codec().string().equals(fourcc), "Failed to find expected codec: " + fourcc + " in " + name + ". got: " + track.codec().string());
                    }
                }
            }

            @Override
            protected void onEnd() {}
        }.start(Media.stream(name));

        assertTrue(foundTag[0], "Did not find any audio tags in " + name);
    }

    @Test
    public void veovera_Opus() throws IOException {
        audioTest("veovera/audio/Opus", "Opus");
    }

    @Test
    public void veovera_Opus_structure() throws IOException {
        OpusHead[] foundHead = {
                null
        };
        int[] codedFrames = {
                0
        };

        new NonSeekableFLVDemuxer() {
            @Override
            protected void onHeader(FLVFileHeader header) {}

            @Override
            protected void onTag(long previousTagSize, FLVTag tag) {
                if (tag.data() instanceof FLVExAudioTagData data) {
                    for (FLVExAudioTrack track : data.tracks()) {
                        if (track.codec().string().equals("Opus") && track.data() instanceof OpusExAudioData opus) {
                            if (opus.isSequenceHeader() && opus.packetData() instanceof OpusHead head) {
                                foundHead[0] = head;
                            } else if (opus.packetData() instanceof OpusCodedData) {
                                codedFrames[0]++;
                            }
                        }
                    }
                }
            }

            @Override
            protected void onEnd() {}
        }.start(Media.stream("veovera/audio/Opus"));

        assertNotNull(foundHead[0], "Did not find an Opus sequence header");
        OpusHead head = foundHead[0];

        // ffprobe / fixture ground truth (veovera/audio/Opus.flv): opus, 48000 Hz,
        // 2 channels. The OpusHead is the RFC 7845 5.1 header, little-endian.
        assertEquals(true, head.hasMagic());
        assertEquals(1, head.version());
        assertEquals(2, head.channelCount());
        assertEquals(312, head.preSkip());
        assertEquals(48000, head.inputSampleRate());
        assertEquals(0, head.outputGain());
        assertEquals(0, head.channelMappingFamily());

        // 10s of audio at a 20ms cadence = 500 Opus packets.
        assertEquals(500, codedFrames[0]);
    }

}
