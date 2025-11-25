package co.casterlabs.flv4j.flv.tags.video;

import java.io.IOException;

import co.casterlabs.flv4j.FLVBVRawSerializable;
import co.casterlabs.flv4j.actionscript.io.ASByteView;
import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.flv.tags.FLVTagData;

// https://rtmp.veriskope.com/pdf/video_file_format_spec_v10.pdf#page=13
// https://veovera.org/docs/enhanced/enhanced-rtmp-v1#defining-additional-video-codecs 
// https://veovera.org/docs/enhanced/enhanced-rtmp-v2#enhanced-video
public interface FLVVideoTagData extends FLVTagData, FLVBVRawSerializable {

    @Override
    public boolean isEx();

    public static FLVVideoTagData parse(ASReader reader, int length) throws IOException {
        byte[] bytes = reader.bytes(length);
        return new FLVStandardVideoTagData(new ASByteView(bytes));
    }

}
