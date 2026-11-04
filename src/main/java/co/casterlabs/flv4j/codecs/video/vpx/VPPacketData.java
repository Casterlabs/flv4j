package co.casterlabs.flv4j.codecs.video.vpx;

import co.casterlabs.flv4j.actionscript.io.ASByteView;

public interface VPPacketData {

    public ASByteView view();

    public static record Invalid(ASByteView view) implements VPPacketData {
    }

}
