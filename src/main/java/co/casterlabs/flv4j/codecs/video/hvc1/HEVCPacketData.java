package co.casterlabs.flv4j.codecs.video.hvc1;

import co.casterlabs.flv4j.actionscript.io.ASByteView;

public interface HEVCPacketData {

    public ASByteView view();

    public static record Invalid(ASByteView view) implements HEVCPacketData {
    }

}
