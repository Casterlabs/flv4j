package co.casterlabs.flv4j.codecs;

import co.casterlabs.flv4j.actionscript.io.ASByteView;

public interface VideoCodecData extends CodecData {

    public int compositionTimeOffset();

    public static record Invalid(ASByteView view) implements VideoCodecData {
        @Override
        public boolean isSequenceHeader() {
            return false;
        }

        @Override
        public int compositionTimeOffset() {
            return 0;
        }

        @Override
        public String toString() {
            return "VideoCodecData[INVALID]";
        }

    }

}
