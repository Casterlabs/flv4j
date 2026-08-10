package co.casterlabs.flv4j.codecs.audio.aac;

import co.casterlabs.flv4j.actionscript.io.ASByteView;
import co.casterlabs.flv4j.codecs.AudioCodecData;

// https://veovera.org/docs/legacy/video-file-format-v10-0-spec.pdf#page=12
public record AACAudioData(
    ASByteView view
) implements AudioCodecData {

    @Override
    public boolean isSequenceHeader() {
        return this.view.u8(0) == 0;
    }

    @Override
    public String toString() {
        return "AACAudioData";
    }

}
