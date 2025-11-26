package co.casterlabs.flv4j.test.flv;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import co.casterlabs.flv4j.flv.FLVFileHeader;
import co.casterlabs.flv4j.flv.muxing.NonSeekableFLVDemuxer;
import co.casterlabs.flv4j.flv.tags.FLVTag;
import co.casterlabs.flv4j.flv.tags.FLVTagType;
import co.casterlabs.flv4j.flv.tags.audio.ex.FLVExAudioTagData;
import lombok.SneakyThrows;

public class FLVMultitrackMuxback {

    @Test
    public void doAudioMuxback() throws IOException {
        new NonSeekableFLVDemuxer() {

            @Override
            protected void onHeader(FLVFileHeader header) {}

            @SneakyThrows
            @Override
            protected void onTag(long previousTagSize, FLVTag tag) {
                if (tag.data() instanceof FLVExAudioTagData data) {
                    FLVExAudioTagData rebuilt = FLVExAudioTagData.from(
                        data.rawType(),
                        data.modifiers(),
                        data.tracks()
                    );

                    FLVTag rebuiltTag = new FLVTag(
                        FLVTagType.AUDIO,
                        tag.timestamp(),
                        tag.streamId(),
                        rebuilt
                    );

//                    System.out.println("Original Tag: " + tag);
//                    System.out.println("Rebuilt Tag:  " + rebuiltTag);
//                    System.out.println();

                    assertTrue(Arrays.equals(rebuiltTag.raw(), tag.raw()), "Rebuilt tag does not match original tag!");
                }
            }

            @Override
            protected void onEnd() {}
        }.start(_Media.stream("veovera/audio/Opus"));
    }

}
