package co.casterlabs.flv4j.flv.tags.audio;

import java.io.IOException;

import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.flv.tags.FLVTagData;
import co.casterlabs.flv4j.flv.tags.audio.ex.FLVExAudioTagData;

// https://veovera.org/docs/enhanced/enhanced-rtmp-v2#enhanced-audio
public interface FLVAudioTagData extends FLVTagData {

    public boolean isEx();

    public static FLVAudioTagData parse(ASReader reader, int length) throws IOException {
        int fb = reader.u8();

        int rawFormat = fb >> 4 & 0b1111;
        if (rawFormat == 9) {
            return FLVExAudioTagData.parse(fb, reader, length);
        } else {
            return FLVStandardAudioTagData.parse(fb, reader, length);
        }
    }

}
