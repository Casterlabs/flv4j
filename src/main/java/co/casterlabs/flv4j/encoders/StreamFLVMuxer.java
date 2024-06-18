package co.casterlabs.flv4j.encoders;

import java.io.IOException;
import java.io.OutputStream;

import co.casterlabs.commons.io.marshalling.PrimitiveMarshall;
import co.casterlabs.flv4j.packets.FLVFileHeader;
import co.casterlabs.flv4j.packets.FLVTag;
import lombok.Getter;
import lombok.NonNull;

public class StreamFLVMuxer {
    private final @Getter FLVFileHeader header;
    private final OutputStream stream;

    private @Getter long bytesWritten = 0;

    private FLVTag previousTag = null;

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
        this.stream.write(this.header.raw());
        this.bytesWritten += this.header.size();
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

        this.stream.write(PrimitiveMarshall.BIG_ENDIAN.intToBytes(this.previousTag == null ? 0 : this.previousTag.size()));
        this.bytesWritten += 4;

        this.stream.write(tag.raw());
        this.bytesWritten += tag.size();

        this.previousTag = tag;
    }

}
