package co.casterlabs.flv4j.codecs;

import co.casterlabs.flv4j.actionscript.io.ASByteView;
import co.casterlabs.flv4j.flv.tags.audio.ex.FLVExAudioCodecData;

public interface AudioCodecData extends CodecData {

    public static record Invalid(ASByteView view) implements AudioCodecData, FLVExAudioCodecData {
        @Override
        public boolean isSequenceHeader() {
            return false;
        }

        @Override
        public String toString() {
            return "AudioCodecData[INVALID]";
        }

    }

}
