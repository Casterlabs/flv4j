package co.casterlabs.flv4j.codecs.audio.opus;

import co.casterlabs.flv4j.actionscript.io.ASByteView;

public interface OpusPacketData {

    public ASByteView view();

    public static record Invalid(ASByteView view) implements OpusPacketData {
    }

}
