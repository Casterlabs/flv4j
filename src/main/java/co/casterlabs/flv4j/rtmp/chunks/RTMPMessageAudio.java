package co.casterlabs.flv4j.rtmp.chunks;

import java.io.IOException;

import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASWriter;
import co.casterlabs.flv4j.flv.tags.audio.FLVAudioTagData;

// https://rtmp.veriskope.com/pdf/rtmp_specification_1.0.pdf#page=26
public record RTMPMessageAudio(FLVAudioTagData payload) implements RTMPMessage {

    @Override
    public int rawType() {
        return 8;
    }

    @Override
    public int size() {
        return this.payload.size();
    }

    @Override
    public void serialize(ASWriter writer) throws IOException {
        this.payload.serialize(writer);
    }

    public static RTMPMessageAudio parse(ASReader reader, int length) throws IOException {
        FLVAudioTagData payload = FLVAudioTagData.parse(reader, length);
        return new RTMPMessageAudio(payload);
    }

}
