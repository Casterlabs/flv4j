package co.casterlabs.flv4j.codecs.audio.opus;

import co.casterlabs.flv4j.actionscript.io.ASByteView;

// https://datatracker.ietf.org/doc/html/rfc6716
public record OpusCodedData(ASByteView view) implements OpusPacketData {

    @Override
    public String toString() {
        return String.format(
            "OpusCodedData[size=%dB]",
            this.view.length()
        );
    }

}
