package co.casterlabs.flv4j.flv.tags.audio.ex;

import java.io.IOException;

import co.casterlabs.flv4j.FLVSerializable;
import co.casterlabs.flv4j.actionscript.io.ASAssert;
import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASSizer;
import co.casterlabs.flv4j.actionscript.io.ASWriter;

// https://veovera.org/docs/enhanced/enhanced-rtmp-v2#enhanced-audio
public record FLVExAudioModifier(
    int rawType,
    int rawNext,
    byte[] data
) implements FLVSerializable {

    public FLVExAudioModifier(int rawType, int rawNext, byte[] data) {
        ASAssert.u4(rawType, "rawType");
        ASAssert.u4(rawNext, "rawNext");
        assert data != null : "data cannot be null";
        this.rawType = rawType;
        this.rawNext = rawNext;
        this.data = data;
    }

    public FLVExAudioModifierType type() {
        return FLVExAudioModifierType.LUT[this.rawType];
    }

    public FLVExAudioPacketType next() {
        return FLVExAudioPacketType.LUT[this.rawNext];
    }

    @Override
    public int size() {
        return new ASSizer()
            .u8()
            .bytes(this.data.length).size;
    }

    @Override
    public void serialize(ASWriter writer) throws IOException {
        int sizeToWrite = this.data.length - 1; // [sic]

        if (sizeToWrite > 255) {
            writer.u8(255);
            writer.u16(sizeToWrite);
        } else {
            writer.u8(sizeToWrite);
        }

        writer.bytes(this.data);
        int fb = this.rawType << 4 | this.rawNext;
        writer.u8(fb);
    }

    public static FLVExAudioModifier parse(ASReader reader) throws IOException {
        int modExDataSize = reader.u8() + 1;
        if (modExDataSize == 256) {
            modExDataSize = reader.u16() + 1;
        }

        byte[] data = reader.bytes(modExDataSize);

        int fb = reader.u8();
        int rawModExType = fb >> 4 & 0b1111;
        int nextRawAudioPacketType = fb & 0b1111;

        return new FLVExAudioModifier(
            rawModExType,
            nextRawAudioPacketType,
            data
        );
    }

    @Override
    public final String toString() {
        return String.format(
            "FLVExModPayload[type=%s (%d), next=%s (%d), data=[length=%d]]",
            this.type(), this.rawType,
            this.next(), this.rawNext,
            this.data.length
        );
    }

}
