package co.casterlabs.flv4j.test.flv;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import co.casterlabs.flv4j.flv.FLVFileHeader;
import co.casterlabs.flv4j.flv.muxing.NonSeekableFLVDemuxer;
import co.casterlabs.flv4j.flv.muxing.StreamFLVMuxer;
import co.casterlabs.flv4j.flv.tags.FLVTag;
import lombok.SneakyThrows;

public class FLV {

    /**
     * To pass, this test MUST demux a source file invoke the onHeader, onTag and
     * onEnd callbacks.
     */
    @Test
    public void demuxerCallbacks() throws IOException {
        boolean[] $results = { // Pointer hax.
                false, // Found header.
                false, // Found tag.
                false  // Finished.
        };

        new NonSeekableFLVDemuxer() {
            @Override
            protected void onHeader(FLVFileHeader header) {
                $results[0] = true;
            }

            @Override
            protected void onTag(long previousTagSize, FLVTag tag) {
                $results[1] = true;
            }

            @Override
            protected void onEnd() {
                $results[2] = true;
            }
        }.start(_Media.stream());

        assertTrue($results[0], "Header not found.");
        assertTrue($results[1], "Tag(s) not found.");
        assertTrue($results[2], "onEnd() not invoked.");
    }

    /**
     * To pass, this test MUST mux a bit-exact copy of it's input.
     */
    @Test
    public void correctness() throws IOException {
        byte[] src = _Media.bytes();
        ByteArrayOutputStream cpy = new ByteArrayOutputStream();

        new NonSeekableFLVDemuxer() {
            private StreamFLVMuxer muxer;

            @SneakyThrows
            @Override
            protected void onHeader(FLVFileHeader header) {
                this.muxer = new StreamFLVMuxer(header, cpy);
            }

            @SneakyThrows
            @Override
            protected void onTag(long previousTagSize, FLVTag tag) {
                this.muxer.write(tag);
            }

            @Override
            protected void onEnd() {}
        }.start(new ByteArrayInputStream(src));

        byte[] copy = cpy.toByteArray();
        assertArrayEquals(src, copy);
    }

}
