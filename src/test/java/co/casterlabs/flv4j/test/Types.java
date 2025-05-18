package co.casterlabs.flv4j.test;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import co.casterlabs.flv4j.muxing.NonSeekableFLVDemuxer;
import co.casterlabs.flv4j.packets.FLVFileHeader;
import co.casterlabs.flv4j.packets.FLVTag;
import co.casterlabs.flv4j.test.util.Media;
import co.casterlabs.flv4j.test.util.TypeAssertions;
import lombok.SneakyThrows;

public class Types {

    /**
     * This test takes the header and any tags from the source file and creates a
     * byte dump of each item. To pass, it MUST successfully re-parse the byte dump
     * of each item.
     */
    @Test
    void muxCorrectness() throws IOException {
        new NonSeekableFLVDemuxer() {
            @SneakyThrows
            @Override
            protected void onHeader(FLVFileHeader header) {
                TypeAssertions.assertReparse(header, FLVFileHeader::parse);
            }

            @SneakyThrows
            @Override
            protected void onTag(long previousTagSize, FLVTag tag) {
                TypeAssertions.assertReparse(tag, FLVTag::parse);
            }
        }.start(Media.stream());
    }

}
