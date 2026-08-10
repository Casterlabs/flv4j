package co.casterlabs.flv4j.rtmp.chunks;

import java.io.IOException;

import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASSizer;
import co.casterlabs.flv4j.actionscript.io.ASWriter;

// https://veovera.org/docs/legacy/rtmp-v1-0-spec.pdf#page=20
/**
 * The client or the server MUST send an acknowledgment to the peer after
 * receiving bytes equal to the {@link RTMPMessageWindowAcknowledgementSize}.
 * The window size is the maximum number of bytes that the sender sends without
 * receiving acknowledgment from the receiver. This message specifies the
 * sequence number, which is the number of the bytes received so far.
 */
public record RTMPMessageAcknowledgement(long bytesReceived) implements RTMPMessage {

    @Override
    public boolean isControl() {
        return true;
    }

    @Override
    public int rawType() {
        return 3;
    }

    @Override
    public int size() {
        return ASSizer.u32; // bytesReceived
    }

    @Override
    public void serialize(ASWriter writer) throws IOException {
        writer.u32(this.bytesReceived % 0xFFFFFFFFL); // [sic]
    }

    public static RTMPMessageAcknowledgement parse(ASReader reader) throws IOException {
        long bytesReceived = reader.u32();
        return new RTMPMessageAcknowledgement(bytesReceived);
    }

}
