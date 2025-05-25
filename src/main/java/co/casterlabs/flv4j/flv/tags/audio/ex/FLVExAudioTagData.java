package co.casterlabs.flv4j.flv.tags.audio.ex;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import co.casterlabs.flv4j.FourCC;
import co.casterlabs.flv4j.actionscript.io.ASAssert;
import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASSizer;
import co.casterlabs.flv4j.actionscript.io.ASWriter;
import co.casterlabs.flv4j.flv.tags.audio.FLVAudioTagData;
import co.casterlabs.flv4j.flv.tags.audio.data.AudioData;

// https://veovera.org/docs/enhanced/enhanced-rtmp-v2#enhanced-audio
public record FLVExAudioTagData(
    int rawType,
    List<FLVExAudioModifier> modifiers,
    int rawMultitrackType, // -1 if not multitrack
    List<FLVExAudioTrack> tracks
) implements FLVAudioTagData {

    public FLVExAudioTagData(int rawType, List<FLVExAudioModifier> modifiers, int rawMultitrackType, List<FLVExAudioTrack> tracks) {
        ASAssert.u4(rawType, "rawAudioPacketType");
        if (rawMultitrackType != -1) ASAssert.u4(rawMultitrackType, "rawMultitrackType");
        assert modifiers != null : "modifiers cannot be null";
        assert tracks != null : "tracks cannot be null";
        this.rawType = rawType;
        this.modifiers = modifiers;
        this.rawMultitrackType = rawMultitrackType;
        this.tracks = tracks;
    }

    public FLVExAudioPacketType type() {
        return FLVExAudioPacketType.LUT[this.rawType];
    }

    public boolean isMultiTrack() {
        return this.rawMultitrackType != -1;
    }

    @Override
    public boolean isEx() {
        return true;
    }

    @Override
    public boolean isSequenceHeader() {
        return this.rawType == FLVExAudioPacketType.SEQUENCE_START.id;
    }

    @Override
    public int size() {
        ASSizer sizer = new ASSizer();

        if (this.modifiers.isEmpty()) {
            if (this.rawMultitrackType == -1) {
                sizer.u8();
            } else {
                sizer.u8();
                sizer.u8();
            }
        } else {
            sizer.u8();

            for (FLVExAudioModifier mod : this.modifiers) {
                sizer.bytes(mod.size());
            }

            if (this.rawMultitrackType != -1) {
                sizer.u8();
            }
        }

        if (this.rawMultitrackType != FLVExAudioMultitrackType.MANY_TRACKS_MANY_CODECS.id) {
            sizer.u32();
        }

        for (FLVExAudioTrack track : this.tracks) {
            if (this.rawMultitrackType != -1) {
                if (this.rawMultitrackType == FLVExAudioMultitrackType.MANY_TRACKS_MANY_CODECS.id) {
                    sizer.u32();
                }

                sizer.u8();

                if (this.rawMultitrackType == FLVExAudioMultitrackType.ONE_TRACK.id) {
                    sizer.u24();
                }
            }

            sizer.bytes(track.data().size());
        }

        return sizer.size;
    }

    @Override
    public void serialize(ASWriter writer) throws IOException {
        if (this.modifiers.isEmpty()) {
            if (this.rawMultitrackType == -1) {
                int fb = 9 << 4 | this.rawType;
                writer.u8(fb);
            } else {
                int fb = 9 << 4 | FLVExAudioPacketType.MULTITRACK.id;
                writer.u8(fb);

                int b = this.rawMultitrackType << 4 | this.rawType;
                writer.u8(b);
            }
        } else {
            int fb = 9 << 4 | FLVExAudioPacketType.MOD_EX.id;
            writer.u8(fb);

            for (FLVExAudioModifier mod : this.modifiers) {
                mod.serialize(writer);
            }

            if (this.rawMultitrackType != -1) {
                int b = this.rawMultitrackType << 4 | this.rawType;
                writer.u8(b);
            }
        }

        if (this.rawMultitrackType != FLVExAudioMultitrackType.MANY_TRACKS_MANY_CODECS.id) {
            FLVExAudioTrack firstTrack = this.tracks.get(0);
            writer.u32(firstTrack.codec().bits());
        }

        for (FLVExAudioTrack track : this.tracks) {
            if (this.rawMultitrackType != -1) {
                int trackHeaderSize = 0;

                if (this.rawMultitrackType == FLVExAudioMultitrackType.MANY_TRACKS_MANY_CODECS.id) {
                    writer.u32(track.codec().bits());
                    trackHeaderSize += 4;
                }

                writer.u8(track.id());
                trackHeaderSize += 1;

                if (this.rawMultitrackType == FLVExAudioMultitrackType.ONE_TRACK.id) {
                    trackHeaderSize += 3;
                    writer.u24(track.data().size() + trackHeaderSize);
                }
            }

            track.data().serialize(writer);
        }
    }

    public static FLVExAudioTagData parse(int fb, ASReader reader, int length) throws IOException {
        int rawAudioPacketType = fb & 0b1111;
        List<FLVExAudioModifier> modifiers = new LinkedList<>();
        while (rawAudioPacketType == FLVExAudioPacketType.MOD_EX.id) {
            FLVExAudioModifier mod = FLVExAudioModifier.parse(reader);
            modifiers.add(mod);
            rawAudioPacketType = mod.rawNext();
        }

        int rawAudioMultitrackType = -1; // -1 if not multitrack
        FourCC codec = null; // Silence the compiler
        if (rawAudioPacketType == FLVExAudioPacketType.MULTITRACK.id) {
            int b = reader.u8();

            rawAudioMultitrackType = b >> 4 & 0b1111;
            rawAudioPacketType = b & 0b1111;
        }

        if (rawAudioMultitrackType != FLVExAudioMultitrackType.MANY_TRACKS_MANY_CODECS.id) {
            codec = FourCC.parse(reader);
        }

        List<FLVExAudioTrack> tracks = new LinkedList<>();
        while (reader.bytesRead() < length) {
            int audioTrackId = 0; // default for single-track audio
            int trackDataSize = length - reader.bytesRead() - 1; // remaining bytes.

            if (rawAudioMultitrackType != -1) {
                int trackHeaderSize = 0;

                if (rawAudioMultitrackType == FLVExAudioMultitrackType.MANY_TRACKS_MANY_CODECS.id) {
                    codec = FourCC.parse(reader);
                    trackHeaderSize += 4;
                }

                audioTrackId = reader.u8();
                trackHeaderSize++;

                if (rawAudioMultitrackType != FLVExAudioMultitrackType.ONE_TRACK.id) {
                    trackHeaderSize += 3;
                    trackDataSize = reader.u24() - trackHeaderSize;
                }
            }

            AudioData data = new AudioData(reader.bytes(trackDataSize));
            tracks.add(new FLVExAudioTrack(codec, audioTrackId, data));
        }

        return new FLVExAudioTagData(rawAudioPacketType, modifiers, rawAudioMultitrackType, tracks);
    }

    @Override
    public final String toString() {
        return String.format(
            "FLVExAudioTagData[format=%s (%d), isSequenceHeader=%b, modifiers=%s, tracks=%s]",
            this.type(), this.rawType,
            this.isSequenceHeader(),
            this.modifiers,
            this.tracks
        );
    }

}
