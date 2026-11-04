package co.casterlabs.flv4j.codecs.video.av01;

import co.casterlabs.flv4j.actionscript.io.ASByteView;

public interface AV1PacketData {

    public ASByteView view();

    public static record Invalid(ASByteView view) implements AV1PacketData {
    }

}
