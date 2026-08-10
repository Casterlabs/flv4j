package co.casterlabs.flv4j.rtmp.chunks;

import java.io.IOException;

import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASWriter;
import co.casterlabs.flv4j.flv.tags.video.FLVVideoTagData;

// https://veovera.org/docs/legacy/rtmp-v1-0-spec.pdf#page=26
public record RTMPMessageVideo(FLVVideoTagData payload) implements RTMPMessage {

    @Override
    public int rawType() {
        return 9;
    }

    @Override
    public int size() {
        return this.payload.size();
    }

    @Override
    public void serialize(ASWriter writer) throws IOException {
        this.payload.serialize(writer);
    }

    public static RTMPMessageVideo parse(ASReader reader, int length) throws IOException {
        FLVVideoTagData payload = FLVVideoTagData.parse(reader, length);
        return new RTMPMessageVideo(payload);
    }

}
