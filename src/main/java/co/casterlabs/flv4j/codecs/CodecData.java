package co.casterlabs.flv4j.codecs;

import co.casterlabs.flv4j.actionscript.io.ASByteView;

public interface CodecData {

    public ASByteView view();

    public boolean isSequenceHeader();

}
