package co.casterlabs.flv4j.test.flv;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import co.casterlabs.flv4j.flv.FLVFileHeader;
import co.casterlabs.flv4j.flv.muxing.NonSeekableFLVDemuxer;
import co.casterlabs.flv4j.flv.tags.FLVTag;
import co.casterlabs.flv4j.flv.tags.audio.FLVAudioFormat;
import co.casterlabs.flv4j.flv.tags.audio.FLVStandardAudioTagData;
import co.casterlabs.flv4j.flv.tags.audio.ex.FLVExAudioTagData;
import co.casterlabs.flv4j.flv.tags.audio.ex.FLVExAudioTrack;

public class FLVAudio {

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
        }.start(_Media.stream(name));

        assertTrue(foundTag[0], "Did not find any audio tags in " + name);
    }

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
        }.start(_Media.stream(name));

        assertTrue(foundTag[0], "Did not find any audio tags in " + name);
    }

    @Test
    public void standard_AAC() throws IOException {
        audioTest("standard/audio/aac", FLVAudioFormat.AAC);
    }

    @Test
    public void standard_ADPCM() throws IOException {
        audioTest("standard/audio/adpcm", FLVAudioFormat.ADPCM);
    }

    @Test
    public void standard_ALAW() throws IOException {
        audioTest("standard/audio/alaw", FLVAudioFormat.G711_ALAW);
    }

//    @Test
//    public void standard_LPCM_PLATFORM_ENDIAN() throws IOException {
//        audioTest("standard/audio/lpcm", FLVAudioFormat.LPCM_PLATFORM_ENDIAN);
//    }

    @Test
    public void standard_LPCM_LE() throws IOException {
        audioTest("standard/audio/lpcm_le", FLVAudioFormat.LPCM_LE);
    }

    @Test
    public void standard_mp3() throws IOException {
        audioTest("standard/audio/mp3", FLVAudioFormat.MP3);
    }

    @Test
    public void standard_MULAW() throws IOException {
        audioTest("standard/audio/mulaw", FLVAudioFormat.G711_MULAW);
    }

    @Test
    public void standard_Nellymoser() throws IOException {
        audioTest("standard/audio/nellymoser", FLVAudioFormat.NELLYMOSER);
    }

    @Test
    public void standard_Nellymoser8Mono() throws IOException {
        audioTest("standard/audio/nellymoser8mono", FLVAudioFormat.NELLYMOSER_8_MONO);
    }

    @Test
    public void standard_Nellymoser16Mono() throws IOException {
        audioTest("standard/audio/nellymoser16mono", FLVAudioFormat.NELLYMOSER_16_MONO);
    }

    @Test
    public void standard_Speex() throws IOException {
        audioTest("standard/audio/speex", FLVAudioFormat.SPEEX);
    }

    @Test
    public void veovera_AC3() throws IOException {
        audioTest("veovera/audio/ac-3", "ac-3");
    }

    @Test
    public void veovera_EAC3() throws IOException {
        audioTest("veovera/audio/ec-3", "ec-3");
    }

    @Test
    public void veovera_Flac() throws IOException {
        audioTest("veovera/audio/fLaC", "fLaC");
    }

    @Test
    public void veovera_Opus() throws IOException {
        audioTest("veovera/audio/Opus", "Opus");
    }

}
