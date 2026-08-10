package co.casterlabs.flv4j.rtmp.chunks;

import java.io.IOException;

import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASSizer;
import co.casterlabs.flv4j.actionscript.io.ASWriter;

// https://veovera.org/docs/legacy/rtmp-v1-0-spec.pdf#page=20
/**
 * The client or the server sends this message to inform the peer of the window
 * size to use between sending acknowledgments. The sender expects
 * acknowledgment from its peer after the sender sends window size bytes. The
 * receiving peer MUST send an {@link RTMPMessageAcknowledgement} after
 * receiving the indicated number of bytes since the last Acknowledgement was
 * sent, or from the beginning of the session if no Acknowledgement has yet been
 * sent.
 */
public record RTMPMessageWindowAcknowledgementSize(long windowSize) implements RTMPMessage {

    @Override
    public boolean isControl() {
        return true;
    }

    @Override
    public int rawType() {
        return 5;
    }

    @Override
    public int size() {
        return ASSizer.u32; // windowSize
    }

    @Override
    public void serialize(ASWriter writer) throws IOException {
        writer.u32(this.windowSize);
    }

    public static RTMPMessageWindowAcknowledgementSize parse(ASReader reader) throws IOException {
        long chunkSize = reader.u32();
        return new RTMPMessageWindowAcknowledgementSize(chunkSize);
    }

}
