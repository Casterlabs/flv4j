package co.casterlabs.flv4j.test.flv;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import co.casterlabs.flv4j.FLVSerializable;
import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.flv.FLVFileHeader;
import co.casterlabs.flv4j.flv.muxing.NonSeekableFLVDemuxer;
import co.casterlabs.flv4j.flv.tags.FLVTag;
import lombok.SneakyThrows;

public class FLVTypes {

    /**
     * This test takes the header and any tags from the source file and creates a
     * byte dump of each item. To pass, it MUST successfully re-parse the byte dump
     * of each item.
     */
    @Test
    public void muxedCorrectness() throws IOException {
        new NonSeekableFLVDemuxer() {
            @SneakyThrows
            @Override
            protected void onHeader(FLVFileHeader header) {
                assertReparse(header, FLVFileHeader::parse);
            }

            @SneakyThrows
            @Override
            protected void onTag(long previousTagSize, FLVTag tag) {
                assertReparse(tag, FLVTag::parse);
            }
        }.start(_Media.stream());
    }

    private static <T extends FLVSerializable> void assertReparse(T src, Parser<T> parser) throws IOException {
        byte[] srcBytes = src.raw();

        T reconstructed = parser.parse(new ASReader(srcBytes, 0, srcBytes.length));
        byte[] reconstructedBytes = reconstructed.raw();

        assertArrayEquals(srcBytes, reconstructedBytes, "Couldn't reparse: " + src.getClass().getSimpleName());
    }

    private static interface Parser<T> {

        public T parse(ASReader src) throws IOException;

    }

}
