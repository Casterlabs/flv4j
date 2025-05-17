package co.casterlabs.flv4j.decoders;

import java.io.IOException;
import java.io.InputStream;

import co.casterlabs.commons.io.marshalling.PrimitiveMarshall;
import co.casterlabs.flv4j.packets.FLVFileHeader;
import co.casterlabs.flv4j.packets.FLVTag;
import co.casterlabs.flv4j.util.ASReader;
import co.casterlabs.flv4j.util.EndOfStreamException;
import co.casterlabs.flv4j.util.ThrowOnMinus1InputStream;
import lombok.Getter;
import lombok.NonNull;

// https://rtmp.veriskope.com/pdf/video_file_format_spec_v10.pdf#page=8
public abstract class NonSeekableFLVDemuxer {
    private @Getter FLVFileHeader header;
    private @Getter long bytesRead = 0;

    public final void start(@NonNull InputStream in) throws IOException {
        ThrowOnMinus1InputStream tm1 = new ThrowOnMinus1InputStream(in);

        try {
            ASReader reader = new ASReader(tm1);

            this.header = FLVFileHeader.parse(reader);
            this.onHeader(this.header);
            this.bytesRead += this.header.size();

            while (true) {
                long previousPacketSizeExclSize = Integer.toUnsignedLong(PrimitiveMarshall.BIG_ENDIAN.bytesToInt(tm1.readNBytes(4)));

                FLVTag tag = FLVTag.parse(reader);
                this.onTag(previousPacketSizeExclSize, tag);
                this.bytesRead += tag.size() + 4;
            }
        } catch (EndOfStreamException eos) {
            this.onEnd();
            return;
        } catch (Throwable t) {
            throw new IOException("Exception caught at position " + this.bytesRead, t);
        }
    }

    protected abstract void onHeader(FLVFileHeader header);

    protected abstract void onTag(long previousTagSize, FLVTag tag);

    protected void onEnd() {}

}
