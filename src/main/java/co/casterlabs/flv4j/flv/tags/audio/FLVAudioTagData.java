package co.casterlabs.flv4j.flv.tags.audio;

import java.io.IOException;

import co.casterlabs.flv4j.FLVBVRawSerializable;
import co.casterlabs.flv4j.actionscript.io.ASByteView;
import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.flv.tags.FLVTagData;
import co.casterlabs.flv4j.flv.tags.audio.ex.FLVExAudioTagData;

// https://veovera.org/docs/enhanced/enhanced-rtmp-v2#enhanced-audio
public interface FLVAudioTagData extends FLVTagData, FLVBVRawSerializable {

    @Override
    public boolean isEx();

    public static FLVAudioTagData parse(ASReader reader, int length) throws IOException {
        ASByteView data = new ASByteView(reader.bytes(length));

        int rawFormat = data.u8(0) >> 4 & 0b1111;
        if (rawFormat == 9) {
            return FLVExAudioTagData.parse(data);
        } else {
            return new FLVStandardAudioTagData(data);
        }
    }

}
