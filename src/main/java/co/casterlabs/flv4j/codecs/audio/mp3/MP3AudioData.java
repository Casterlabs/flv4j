package co.casterlabs.flv4j.codecs.audio.mp3;

import co.casterlabs.flv4j.actionscript.io.ASByteView;
import co.casterlabs.flv4j.codecs.AudioCodecData;

// https://veovera.org/docs/legacy/video-file-format-v10-0-spec.pdf#page=10
public record MP3AudioData(
    ASByteView view
) implements AudioCodecData {

    public MP3Frame[] frames() {
        return MP3Frame.frames(this.view);
    }

    @Override
    public boolean isSequenceHeader() {
        return false; // every tag is raw frames.
    }

    @Override
    public String toString() {
        MP3Frame[] frames = this.frames();
        return String.format(
            "MP3AudioData[frames=%d, size=%dB, first=%s]",
            frames.length,
            this.view.length(),
            frames.length > 0 ? frames[0].header() : "<empty>"
        );
    }

    public MP3ExAudioData toEx() {
        return new MP3ExAudioData(this.view);
    }

}
