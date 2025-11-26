package co.casterlabs.flv4j.codecs.video.avc1;

import co.casterlabs.flv4j.actionscript.io.ASByteView;

public interface AVCPacketData {

    public ASByteView view();

    public static record Invalid(ASByteView view) implements AVCPacketData {
    }

}
