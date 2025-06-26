package co.casterlabs.flv4j.rtmp.chunks;

import java.io.IOException;

import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASSizer;
import co.casterlabs.flv4j.actionscript.io.ASWriter;
import co.casterlabs.flv4j.rtmp.chunks.control.RTMPControlMessage;
import co.casterlabs.flv4j.rtmp.chunks.control.RTMPPingRequestControlMessage;
import co.casterlabs.flv4j.rtmp.chunks.control.RTMPPingResponseControlMessage;
import co.casterlabs.flv4j.rtmp.chunks.control.RTMPRawControlMessage;
import co.casterlabs.flv4j.rtmp.chunks.control.RTMPSetBufferLengthControlMessage;
import co.casterlabs.flv4j.rtmp.chunks.control.RTMPStreamBeginControlMessage;
import co.casterlabs.flv4j.rtmp.chunks.control.RTMPStreamDryControlMessage;
import co.casterlabs.flv4j.rtmp.chunks.control.RTMPStreamEOFControlMessage;
import co.casterlabs.flv4j.rtmp.chunks.control.RTMPStreamIsRecordedControlMessage;

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
public record RTMPMessageUserControl(int eventType, RTMPControlMessage eventData) implements RTMPMessage {

    @Override
    public int rawType() {
        return 4;
    }

    @Override
    public int size() {
        return new ASSizer()
            .u16()
            .bytes(this.eventData.size()).size;
    }

    @Override
    public void serialize(ASWriter writer) throws IOException {
        writer.u16(this.eventType);
        this.eventData.serialize(writer);
    }

    public static RTMPMessageUserControl parse(ASReader reader, int length) throws IOException {
        int eventType = reader.u16();
        int dataLen = length - 2;

        // https://rtmp.veriskope.com/docs/spec/#717user-control-message-events
        RTMPControlMessage eventData = switch (eventType) {
            case 0 -> RTMPStreamBeginControlMessage.parse(reader, dataLen);
            case 1 -> RTMPStreamEOFControlMessage.parse(reader, dataLen);
            case 2 -> RTMPStreamDryControlMessage.parse(reader, dataLen);
            case 3 -> RTMPSetBufferLengthControlMessage.parse(reader, dataLen);
            case 4 -> RTMPStreamIsRecordedControlMessage.parse(reader, dataLen);
            // no 5?
            case 6 -> RTMPPingRequestControlMessage.parse(reader, dataLen);
            case 7 -> RTMPPingResponseControlMessage.parse(reader, dataLen);
            default -> new RTMPRawControlMessage(reader.bytes(dataLen));
        };

        return new RTMPMessageUserControl(eventType, eventData);
    }

}
