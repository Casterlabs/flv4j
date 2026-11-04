package co.casterlabs.flv4j.codecs.audio.mp3;

import co.casterlabs.flv4j.actionscript.io.ASByteView;

public record MP3Frame(
    MP3FrameHeader header,
    ASByteView view
) {

    static MP3Frame[] frames(ASByteView view) {
        // Walks the raw frames. Stops at the first offset that is not a valid
        // frame header.
        int count = 0;
        int offset = 0;
        while (offset + 4 <= view.length()) {
            MP3FrameHeader header = new MP3FrameHeader(view.slice(offset, 4));
            int size = header.frameSize();
            if (!header.sync() || size < 4 || offset + size > view.length()) {
                break;
            }
            count++;
            offset += size;
        }

        MP3Frame[] frames = new MP3Frame[count];
        offset = 0;
        for (int i = 0; i < frames.length; i++) {
            MP3FrameHeader header = new MP3FrameHeader(view.slice(offset, 4));
            int size = header.frameSize();
            frames[i] = new MP3Frame(header, view.slice(offset, size));
            offset += size;
        }
        return frames;
    }

}
