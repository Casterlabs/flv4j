package co.casterlabs.flv4j.encoders;

import java.io.IOException;
import java.io.OutputStream;

import co.casterlabs.flv4j.packets.FLVFileHeader;
import co.casterlabs.flv4j.packets.FLVTag;
import co.casterlabs.flv4j.util.ASWriter;
import lombok.Getter;
import lombok.NonNull;

// https://rtmp.veriskope.com/pdf/video_file_format_spec_v10.pdf#page=8
public class StreamFLVMuxer {
    private final @Getter FLVFileHeader header;
    private final OutputStream stream;

    private @Getter long bytesWritten = 0;

    /**
     * This allows you to rewrite the timestamps from another source.
     * 
     * You probably want this to be a negative number.
     * 
     * @implNote Tags with timestamps that end up being less than 0 will be forcibly
     *           set to 0 to prevent timing errors.
     */
    public long timestampOffset = 0;

    public StreamFLVMuxer(@NonNull FLVFileHeader header, @NonNull OutputStream stream) throws IOException {
        this.header = header;
        this.stream = stream;

        // This is the first packet. Write out the header.
        this.header.serialize(stream);
        this.bytesWritten += this.header.size();

        ASWriter.u32(this.stream, 0); // initial previousTagSize
        this.bytesWritten += 4;
    }

    public synchronized void write(FLVTag tag) throws IOException {
        if (this.timestampOffset != 0) {
            tag = new FLVTag(
                // Modify the tag with our new ts offset.
                tag.type(),
                tag.payloadSize(),
                Math.max(tag.timestamp() + this.timestampOffset, 0), // Min. value of 0!
                tag.streamId(),
                tag.payload()
//                tag.payload().clone() // Useful for testing raw() generation.
            );
        }

        int tagSize = tag.size();

        tag.serialize(this.stream);
        this.bytesWritten += tagSize;

        ASWriter.u32(this.stream, tagSize); // previousTagSize, also acts like the trailer.
        this.bytesWritten += 4;
    }

}
