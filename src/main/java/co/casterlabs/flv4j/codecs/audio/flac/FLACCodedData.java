package co.casterlabs.flv4j.codecs.audio.flac;

import co.casterlabs.flv4j.actionscript.io.ASByteView;

// https://www.rfc-editor.org/info/rfc9639/#section-9
public record FLACCodedData(ASByteView view) implements FLACPacketData {

    public FLACFrameHeader header() {
        return FLACFrameHeader.from(this.view);
    }

    @Override
    public String toString() {
        return String.format(
            "FlacCodedData[size=%dB, %s]",
            this.view.length(),
            this.header()
        );
    }

}
