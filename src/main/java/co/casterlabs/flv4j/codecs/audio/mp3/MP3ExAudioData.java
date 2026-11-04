package co.casterlabs.flv4j.codecs.audio.mp3;

import co.casterlabs.flv4j.actionscript.io.ASByteView;
import co.casterlabs.flv4j.flv.tags.audio.ex.FLVExAudioCodecData;

// https://veovera.org/docs/enhanced/enhanced-rtmp-v2#enhanced-audio
public record MP3ExAudioData(
    ASByteView view
) implements FLVExAudioCodecData {

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
            "MP3ExAudioData[frames=%d, size=%dB, first=%s]",
            frames.length,
            this.view.length(),
            frames.length > 0 ? frames[0].header() : "<empty>"
        );
    }

    public MP3AudioData toStd() {
        return new MP3AudioData(this.view);
    }

}
