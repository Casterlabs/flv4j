package co.casterlabs.flv4j.rtmp.chunks;

import java.io.IOException;

import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASSizer;
import co.casterlabs.flv4j.actionscript.io.ASWriter;

// https://rtmp.veriskope.com/pdf/rtmp_specification_1.0.pdf#page=20
/**
 * The client or the server MUST send an acknowledgment to the peer after
 * receiving bytes equal to the {@link RTMPMessageWindowAcknowledgementSize}.
 * The window size is the maximum number of bytes that the sender sends without
 * receiving acknowledgment from the receiver. This message specifies the
 * sequence number, which is the number of the bytes received so far.
 */
public record RTMPMessageAcknowledgement(long bytesReceived) implements RTMPMessage {
    private static final int SIZE = new ASSizer().u32().size;

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
        return SIZE;
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
