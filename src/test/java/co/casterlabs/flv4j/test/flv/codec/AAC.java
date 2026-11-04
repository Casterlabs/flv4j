package co.casterlabs.flv4j.test.flv.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import co.casterlabs.flv4j.actionscript.io.ASByteView;
import co.casterlabs.flv4j.codecs.audio.aac.AACAudioData;
import co.casterlabs.flv4j.codecs.audio.aac.AACAudioSpecificConfig;
import co.casterlabs.flv4j.flv.FLVFileHeader;
import co.casterlabs.flv4j.flv.muxing.NonSeekableFLVDemuxer;
import co.casterlabs.flv4j.flv.tags.FLVTag;
import co.casterlabs.flv4j.flv.tags.audio.FLVAudioFormat;
import co.casterlabs.flv4j.flv.tags.audio.FLVStandardAudioTagData;
import co.casterlabs.flv4j.test.Media;

public class AAC {

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
    public void standard_AAC() throws IOException {
        audioTest("standard/audio/aac", FLVAudioFormat.AAC);
    }

    @Test
    public void standard_AAC_structure() throws IOException {
        AACAudioSpecificConfig[] foundAsc = {
                null
        };

        new NonSeekableFLVDemuxer() {
            @Override
            protected void onHeader(FLVFileHeader header) {}

            @Override
            protected void onTag(long previousTagSize, FLVTag tag) {
                if (tag.data() instanceof FLVStandardAudioTagData data
                    && data.data() instanceof AACAudioData aac && aac.isSequenceHeader()) {
                    foundAsc[0] = (AACAudioSpecificConfig) aac.packetData();
                }
            }

            @Override
            protected void onEnd() {}
        }.start(Media.stream("standard/audio/aac"));

        assertNotNull(foundAsc[0], "Did not find an AAC sequence header");
        AACAudioSpecificConfig asc = foundAsc[0];

        // ffprobe ground truth (aac.flv): aac, LC, 48000 Hz, 2 channels.
        assertEquals(2, asc.audioObjectType()); // AAC-LC
        assertEquals(3, asc.sampleRateIndex());
        assertEquals(48000, asc.sampleRate());
        assertEquals(2, asc.channelConfig());
        assertEquals(2, asc.channelCount());
        assertEquals(false, asc.frameLengthFlag());
        assertEquals(false, asc.dependsOnCoreCoder());
        assertEquals(false, asc.extensionFlag());
    }

    @Test
    public void standard_AAC_explicit_sample_rate() {
        // Hand-built 5-byte AudioSpecificConfig: aot=2 (LC),
        // samplingFrequencyIndex=0xf with the 24-bit samplingFrequency field
        // (2400 = 19200 Hz shifted right by 3 bits), channelConfiguration=2,
        // all flags clear.
        byte[] explicit = {
                (byte) 0x17, // 00010 111: aot=2, sfi top 3 bits
                (byte) 0x80, // sfi low bit + freq bits 23..17
                (byte) 0x04, // freq bits 16..9
                (byte) 0xB0, // freq bits 8..1
                (byte) 0x10 // freq bit 0 + channelConfiguration=2
        };
        AACAudioSpecificConfig asc = new AACAudioSpecificConfig(new ASByteView(explicit));

        assertEquals(2, asc.audioObjectType()); // AAC-LC
        assertEquals(0x0F, asc.sampleRateIndex());
        assertEquals(true, asc.hasExplicitSampleRate());
        assertEquals(19200, asc.sampleRate()); // 2400 << 3
        assertEquals(2, asc.channelConfig());
        assertEquals(2, asc.channelCount());
        assertEquals(false, asc.frameLengthFlag());
        assertEquals(false, asc.dependsOnCoreCoder());
        assertEquals(false, asc.extensionFlag());

        // A view too short to carry the 24-bit field keeps the 2-byte layout
        // and reports rate 0.
        byte[] shortView = {
                (byte) 0x17,
                (byte) 0x8F
        };
        AACAudioSpecificConfig truncated = new AACAudioSpecificConfig(new ASByteView(shortView));
        assertEquals(0x0F, truncated.sampleRateIndex());
        assertEquals(false, truncated.hasExplicitSampleRate());
        assertEquals(0, truncated.sampleRate());
    }

}
