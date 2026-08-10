package co.casterlabs.flv4j.flv.tags.video;

import java.io.IOException;

import co.casterlabs.flv4j.FLVBVRawSerializable;
import co.casterlabs.flv4j.actionscript.io.ASByteView;
import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.flv.tags.FLVTagData;
import co.casterlabs.flv4j.flv.tags.video.ex.FLVExVideoTagData;

// https://rtmp.veriskope.com/pdf/video_file_format_spec_v10.pdf#page=13
// https://veovera.org/docs/enhanced/enhanced-rtmp-v1#defining-additional-video-codecs 
// https://veovera.org/docs/enhanced/enhanced-rtmp-v2#enhanced-video
public interface FLVVideoTagData extends FLVTagData, FLVBVRawSerializable {

    @Override
    public boolean isEx();

    public static FLVVideoTagData parse(ASReader reader, int length) throws IOException {
        ASByteView data = new ASByteView(reader.bytes(length));

        boolean isExVideoHeader = (data.u8(0) & 0b10000000) != 0;
        if (isExVideoHeader) {
            return FLVExVideoTagData.parse(data);
        } else {
            return new FLVStandardVideoTagData(data);
        }
    }

}
