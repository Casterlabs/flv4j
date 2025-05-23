package co.casterlabs.flv4j.rtmp.chunks;

import java.io.IOException;

import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASSizer;
import co.casterlabs.flv4j.actionscript.io.ASWriter;

// https://rtmp.veriskope.com/pdf/rtmp_specification_1.0.pdf#page=24
/**
 * The client or the server sends this message to notify the peer about the user
 * control events. This message carries Event type and Event data.
 * 
 * @apiNote User Control messages SHOULD use message stream ID 0 (known as the
 *          control stream) and, when sent over RTMP Chunk Stream, be sent on
 *          chunk stream ID 2. User Control messages are effective at the point
 *          they are received in the stream; their timestamps are ignored.
 */
public record RTMPMessageUserControl(int eventType, byte[] eventData) implements RTMPMessage {

    @Override
    public int rawType() {
        return 4;
    }

    @Override
    public int size() {
        return new ASSizer()
            .u16()
            .bytes(this.eventData.length).size;
    }

    @Override
    public void serialize(ASWriter writer) throws IOException {
        writer.u16(this.eventType);
        writer.bytes(this.eventData);
    }

    public static RTMPMessageUserControl parse(ASReader reader, int length) throws IOException {
        int eventType = reader.u16();
        byte[] eventData = reader.bytes(length - 2);

        return new RTMPMessageUserControl(eventType, eventData);
    }

}
