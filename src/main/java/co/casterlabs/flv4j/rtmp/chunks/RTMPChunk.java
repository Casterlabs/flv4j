package co.casterlabs.flv4j.rtmp.chunks;

// https://veovera.org/docs/legacy/rtmp-v1-0-spec.pdf#page=13
public record RTMPChunk<M extends RTMPMessage>(
    int timestamp,
    int chunkStreamId,
    int messageTypeId,
    long messageStreamId,
    M message
) {

    @Override
    public final String toString() {
        return String.format(
            "RTMPChunk[timestamp=%d, chunkStreamId=%d, messageTypeId=%d, messageStreamId=%d, message=[length=%d, data=%s]]",
            this.timestamp,
            this.chunkStreamId,
            this.messageTypeId,
            this.messageStreamId,
            this.message.size(),
            this.message
        );
    }

}
