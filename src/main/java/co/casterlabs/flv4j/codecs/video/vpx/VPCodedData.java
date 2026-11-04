package co.casterlabs.flv4j.codecs.video.vpx;

import co.casterlabs.flv4j.actionscript.io.ASByteView;

public record VPCodedData(ASByteView view) implements VPPacketData {

    @Override
    public String toString() {
        return String.format(
            "VPCodedData[size=%dB]",
            this.view.length()
        );
    }

}
