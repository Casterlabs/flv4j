package co.casterlabs.flv4j.rtmp.chunks;

import java.io.IOException;

import co.casterlabs.flv4j.FLVSerializable;
import co.casterlabs.flv4j.actionscript.io.ASReader;

// https://rtmp.veriskope.com/pdf/rtmp_specification_1.0.pdf#page=19
// https://rtmp.veriskope.com/pdf/rtmp_specification_1.0.pdf#page=24
public interface RTMPMessage extends FLVSerializable {

    public default boolean isControl() {
        return false;
    }

    public int rawType();

    public static RTMPMessage parse(int type, int length, ASReader reader) throws IOException {
        reader = reader.limited(length);

        return switch (type) {
            // Protocol
            case 1 -> RTMPMessageChunkSize.parse(reader);
            case 2 -> RTMPMessageAbort.parse(reader);
            case 3 -> RTMPMessageAcknowledgement.parse(reader);
            case 4 -> RTMPMessageUserControl.parse(reader, length);
            case 5 -> RTMPMessageWindowAcknowledgementSize.parse(reader);
            case 6 -> RTMPMessageSetPeerBandwidth.parse(reader);

            // Data
            case 8 -> RTMPMessageAudio.parse(reader, length);
            case 9 -> RTMPMessageVideo.parse(reader, length);

            case 18 -> RTMPMessageData0.parse(reader);

            // Commands
            case 20 -> RTMPMessageCommand0.parse(reader);

            // ?
            default -> new RTMPMessageUnknown(type, reader.bytes(length));
        };
    }

}
