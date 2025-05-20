package co.casterlabs.flv4j.flv.muxing;

import java.io.IOException;
import java.io.InputStream;

import co.casterlabs.flv4j.EndOfStreamException;
import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.flv.packets.FLVFileHeader;
import co.casterlabs.flv4j.flv.packets.FLVTag;
import lombok.Getter;
import lombok.NonNull;

// https://rtmp.veriskope.com/pdf/video_file_format_spec_v10.pdf#page=8
public abstract class NonSeekableFLVDemuxer {
    private @Getter FLVFileHeader header;
    private @Getter long bytesRead = 0;

    public final void start(@NonNull InputStream in) throws IOException {
        this.start(new ASReader(in));
    }

    public final void start(@NonNull ASReader reader) throws IOException {
        try {
            this.header = FLVFileHeader.parse(reader);
            this.onHeader(this.header);
            this.bytesRead += this.header.size();

            while (true) {
                long previousPacketSizeExclSize = reader.u32();

                FLVTag tag = FLVTag.parse(reader);
                this.onTag(previousPacketSizeExclSize, tag);
                this.bytesRead += tag.size() + 4;
            }
        } catch (EndOfStreamException eos) {
            this.onEnd();
            return;
        }
    }

    protected abstract void onHeader(FLVFileHeader header);

    protected abstract void onTag(long previousTagSize, FLVTag tag);

    protected void onEnd() {}

}
