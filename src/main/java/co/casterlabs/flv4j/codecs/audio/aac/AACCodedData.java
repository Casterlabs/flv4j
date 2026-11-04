package co.casterlabs.flv4j.codecs.audio.aac;

import co.casterlabs.flv4j.actionscript.io.ASByteView;

public record AACCodedData(ASByteView view) implements AACPacketData {

    @Override
    public String toString() {
        return String.format(
            "AACCodedData[size=%dB]",
            this.view.length()
        );
    }

}
