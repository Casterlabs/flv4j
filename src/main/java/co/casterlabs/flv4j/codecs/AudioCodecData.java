package co.casterlabs.flv4j.codecs;

import co.casterlabs.flv4j.actionscript.io.ASByteView;

public interface AudioCodecData extends CodecData {

    public static record Invalid(ASByteView view) implements AudioCodecData {
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
