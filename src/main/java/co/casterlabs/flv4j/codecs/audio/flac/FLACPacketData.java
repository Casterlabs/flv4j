package co.casterlabs.flv4j.codecs.audio.flac;

import co.casterlabs.flv4j.actionscript.io.ASByteView;

public interface FLACPacketData {

    public ASByteView view();

    public static record Invalid(ASByteView view) implements FLACPacketData {
    }

}
