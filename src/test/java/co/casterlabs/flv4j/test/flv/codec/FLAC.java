package co.casterlabs.flv4j.test.flv.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import co.casterlabs.flv4j.actionscript.io.ASByteView;
import co.casterlabs.flv4j.codecs.audio.flac.FLACCodedData;
import co.casterlabs.flv4j.codecs.audio.flac.FLACExAudioData;
import co.casterlabs.flv4j.codecs.audio.flac.FLACFrameHeader;
import co.casterlabs.flv4j.codecs.audio.flac.FLACStreamInfo;
import co.casterlabs.flv4j.flv.FLVFileHeader;
import co.casterlabs.flv4j.flv.muxing.NonSeekableFLVDemuxer;
import co.casterlabs.flv4j.flv.tags.FLVTag;
import co.casterlabs.flv4j.flv.tags.audio.ex.FLVExAudioTagData;
import co.casterlabs.flv4j.flv.tags.audio.ex.FLVExAudioTrack;
import co.casterlabs.flv4j.test.Media;

public class FLAC {

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
    public void veovera_Flac() throws IOException {
        audioTest("veovera/audio/fLaC", "fLaC");
    }

    @Test
    public void veovera_Flac_structure() throws IOException {
        FLACStreamInfo[] foundInfo = {
                null
        };
        FLACFrameHeader[] firstHeader = {
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
                        if (track.codec().string().equals("fLaC") && track.data() instanceof FLACExAudioData flac) {
                            if (flac.isSequenceHeader() && flac.packetData() instanceof FLACStreamInfo info) {
                                if (foundInfo[0] == null) {
                                    foundInfo[0] = info;
                                }
                            } else if (flac.packetData() instanceof FLACCodedData coded) {
                                if (firstHeader[0] == null && coded.view().length() > 0) {
                                    firstHeader[0] = FLACFrameHeader.from(coded.view());
                                }
                                codedFrames[0]++;
                            }
                        }
                    }
                }
            }

            @Override
            protected void onEnd() {}
        }.start(Media.stream("veovera/audio/fLaC"));

        assertNotNull(foundInfo[0], "Did not find a FLAC sequence header");
        FLACStreamInfo info = foundInfo[0];

        // ffprobe / fixture ground truth (veovera/audio/fLaC.flv): flac, 48000 Hz,
        // 2 channels, 24-bit, fixed blocksize 4608. The raw 34-byte STREAMINFO.
        assertEquals(4608, info.minBlocksize());
        assertEquals(4608, info.maxBlocksize());
        assertEquals(0, info.minFrameSize());
        assertEquals(28250, info.maxFrameSize());
        assertEquals(48000, info.sampleRate());
        assertEquals(2, info.channels());
        assertEquals(24, info.bitsPerSample());
        assertEquals(0, info.totalSamples());
        assertTrue(Arrays.equals(info.md5(), new byte[16]));

        assertNotNull(firstHeader[0], "Did not find a FLAC frame header");
        FLACFrameHeader header = firstHeader[0];

        // The first frame's header (FFmpeg n7.1 ff_flac_decode_frame_header; the
        // header CRC-8 validates every real frame in the fixture).
        assertEquals(4608, header.blockSize());
        assertEquals(48000, header.sampleRate());
        assertEquals(2, header.channels());
        assertEquals(24, header.bitsPerSample());
        assertEquals(0, header.frameNumber());
        assertEquals(false, header.variableSize());
        assertEquals(0, header.reservedZeroBit());
        assertEquals(6, header.headerLength());

        // 10s of audio at 48000 Hz with 4608-sample frames = 104 real frames
        // (479232 samples = 9.984s). The fixture's 105th coded tag is a trailing
        // empty (silence) tag with no track payload, so the demuxer yields 104
        // frame-bearing coded tags.
        assertEquals(104, codedFrames[0]);
    }

    @Test
    public void veovera_Flac_multibyte_frame_numbers() {
        // Hand-packed frame headers in the class's field order (192-block /
        // 48000 / 1ch / 16-bit, CRC zero) exercising the 2-byte and 3-byte
        // UTF-8 frame number encodings: 300 = C4 AC, 40000 = E9 B1 80.
        byte[] frame2 = {
                (byte) 0xFF,
                (byte) 0xF9,
                0x1A,
                0x08,
                (byte) 0xC4,
                (byte) 0xAC,
                0x00
        };
        FLACFrameHeader header2 = FLACFrameHeader.from(new ASByteView(frame2));
        assertEquals(300, header2.frameNumber());
        assertEquals(192, header2.blockSize());
        assertEquals(48000, header2.sampleRate());
        assertEquals(16, header2.bitsPerSample());
        assertEquals(1, header2.channels());
        assertEquals(true, header2.variableSize());
        assertEquals(0, header2.reservedZeroBit());
        assertEquals(7, header2.headerLength());

        byte[] frame3 = {
                (byte) 0xFF,
                (byte) 0xF9,
                0x1A,
                0x08,
                (byte) 0xE9,
                (byte) 0xB1,
                (byte) 0x80,
                0x00
        };
        FLACFrameHeader header3 = FLACFrameHeader.from(new ASByteView(frame3));
        assertEquals(40000, header3.frameNumber());
        assertEquals(192, header3.blockSize());
        assertEquals(48000, header3.sampleRate());
        assertEquals(16, header3.bitsPerSample());
        assertEquals(1, header3.channels());
        assertEquals(true, header3.variableSize());
        assertEquals(0, header3.reservedZeroBit());
        assertEquals(8, header3.headerLength());
    }

}
