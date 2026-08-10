package co.casterlabs.flv4j.rtmp.chunks;

import java.io.IOException;

import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASSizer;
import co.casterlabs.flv4j.actionscript.io.ASWriter;

// https://veovera.org/docs/legacy/rtmp-v1-0-spec.pdf#page=19
/**
 * Used to notify the peer if it is waiting for chunks to complete a message,
 * then to discard the partially received message over a chunk stream. The peer
 * receives the chunk stream ID as this protocol message’s payload. An
 * application may send this message when closing in order to indicate that
 * further processing of the messages is not required.
 */
public record RTMPMessageAbort(long streamId) implements RTMPMessage {

    @Override
    public boolean isControl() {
        return true;
    }

    @Override
    public int rawType() {
        return 2;
    }

    @Override
    public int size() {
        return ASSizer.u32; // streamId
    }

    @Override
    public void serialize(ASWriter writer) throws IOException {
        writer.u32(this.streamId);
    }

    public static RTMPMessageAbort parse(ASReader reader) throws IOException {
        long chunkSize = reader.u32();
        return new RTMPMessageAbort(chunkSize);
    }

}
