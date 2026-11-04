package co.casterlabs.flv4j.codecs.audio.aac;

import co.casterlabs.flv4j.actionscript.io.ASByteView;

public interface AACPacketData {

    public ASByteView view();

    public static record Invalid(ASByteView view) implements AACPacketData {
    }

}
