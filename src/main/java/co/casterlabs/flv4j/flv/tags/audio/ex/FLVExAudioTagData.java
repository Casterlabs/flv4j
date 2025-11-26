package co.casterlabs.flv4j.flv.tags.audio.ex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import co.casterlabs.flv4j.FourCC;
import co.casterlabs.flv4j.actionscript.io.ASByteView;
import co.casterlabs.flv4j.actionscript.io.ASWriter;
import co.casterlabs.flv4j.codecs.AudioCodecData;
import co.casterlabs.flv4j.flv.tags.audio.FLVAudioTagData;
import lombok.NonNull;
import lombok.SneakyThrows;

// https://veovera.org/docs/enhanced/enhanced-rtmp-v2#enhanced-audio
public record FLVExAudioTagData(
    ASByteView view, // Passthrough ;)
    int rawType,
    FLVExAudioModifier[] modifiers,
    FLVExAudioTrack[] tracks
) implements FLVAudioTagData {

    public FLVExAudioPacketType type() {
        return FLVExAudioPacketType.LUT[this.rawType];
    }

    @Override
    public boolean isEx() {
        return true;
    }

    @Override
    public boolean isSequenceHeader() {
        return this.rawType == FLVExAudioPacketType.SEQUENCE_START.id;
    }

    public static FLVExAudioTagData from(@NonNull FLVExAudioPacketType type, FLVExAudioModifier[] modifiers, FLVExAudioTrack... tracks) {
        return from(type.id, modifiers, tracks);
    }

    @SneakyThrows
    public static FLVExAudioTagData from(int rawType, @NonNull FLVExAudioModifier[] modifiers, @NonNull FLVExAudioTrack... tracks) {
        assert tracks.length > 0 : "At least one track must be provided.";

        // We have to serialize the content to an first.
        // That way we have valid data to pass into the constructor.

        int rawMultitrackType = -1; // -1 if not multitrack
        if (tracks.length == 1) {
            // Track 0 is defined by the spec as the "main" track.
            // If it's not in here, then we can assume that Track 0 is using a Standard tag.
            // So, we have to use a "multitrack" signal, even though there's only one track.
            // Weird spec design I guess. But it works so we have to play along.
            boolean hasTrack0 = tracks[0].id() == 0;
            if (!hasTrack0) {
                rawMultitrackType = FLVExAudioMultitrackType.ONE_TRACK.id;
            }
        } else {
            rawMultitrackType = FLVExAudioMultitrackType.MANY_TRACKS.id;

            FourCC firstCodec = tracks[0].codec();
            for (FLVExAudioTrack track : tracks) {
                if (track.codec().bits() != firstCodec.bits()) {
                    rawMultitrackType = FLVExAudioMultitrackType.MANY_TRACKS_MANY_CODECS.id;
                    break;
                }
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ASWriter writer = new ASWriter(baos);

        if (modifiers.length == 0) {
            if (rawMultitrackType == -1) {
                writer.u8(9 << 4 | rawType);
            } else {
                writer.u8(9 << 4 | FLVExAudioPacketType.MULTITRACK.id);
                writer.u8(rawMultitrackType << 4 | rawType);
            }
        } else {
            writer.u8(9 << 4 | FLVExAudioPacketType.MOD_EX.id);

            for (int i = 0; i < modifiers.length; i++) {
                FLVExAudioModifier mod = modifiers[i];
                boolean isLastMod = (i == modifiers.length - 1);

                int modDataSize = mod.data().length;
                if (modDataSize > 255) {
                    writer.u8(255);
                    writer.u16(modDataSize - 1);
                } else {
                    writer.u8(modDataSize - 1);
                }

                writer.bytes(mod.data());

                if (isLastMod) {
                    if (rawMultitrackType == -1) {
                        writer.u8(mod.rawType() << 4 | rawType);
                    } else {
                        writer.u8(mod.rawType() << 4 | FLVExAudioPacketType.MULTITRACK.id);
                        writer.u8(rawMultitrackType << 4 | rawType);
                    }
                } else {
                    writer.u8(mod.rawType() << 4 | FLVExAudioPacketType.MOD_EX.id);
                }
            }
        }

        if (rawMultitrackType != FLVExAudioMultitrackType.MANY_TRACKS_MANY_CODECS.id) {
            writer.u32(tracks[0].codec().bits());
        }

        for (FLVExAudioTrack track : tracks) {
            if (rawMultitrackType != -1) {
                if (rawMultitrackType == FLVExAudioMultitrackType.MANY_TRACKS_MANY_CODECS.id) {
                    writer.u32(track.codec().bits());
                }

                writer.u8(track.id());

                if (rawMultitrackType != FLVExAudioMultitrackType.ONE_TRACK.id) {
                    writer.u24(track.data().view().length());
                }
            }
            writer.bytes(track.data().view());
        }

        return new FLVExAudioTagData(
            new ASByteView(baos.toByteArray()),
            rawType,
            modifiers,
            tracks
        );
    }

    public static FLVExAudioTagData parse(ASByteView data) throws IOException {
        int offset = 0;
        int rawAudioPacketType = data.u8(offset++) & 0b1111;

        List<FLVExAudioModifier> modifiers = new LinkedList<>();
        while (rawAudioPacketType == FLVExAudioPacketType.MOD_EX.id) {
            int modDataSize = data.u8(offset++) + 1;
            if (modDataSize == 256) {
                modDataSize += data.u16(offset);
                offset += 2;
            }

            byte[] modExData = data.bytes(offset, modDataSize);
            offset += modDataSize;

            int modType = data.u8(offset) >> 4 & 0b1111;
            rawAudioPacketType = data.u8(offset) & 0b1111;
            offset++;

            modifiers.add(new FLVExAudioModifier(modType, modExData));
        }

        int rawAudioMultitrackType = -1; // -1 if not multitrack
        FourCC codec = null; // Silence the compiler

        if (rawAudioPacketType == FLVExAudioPacketType.MULTITRACK.id) {
            rawAudioMultitrackType = data.u8(offset) >> 4 & 0b1111;
            rawAudioPacketType = data.u8(offset) & 0b1111; // NB: MUST not be MULTITRACK
            offset++;

            if (rawAudioMultitrackType != FLVExAudioMultitrackType.MANY_TRACKS_MANY_CODECS.id) {
                codec = new FourCC(data.u32(offset));
                offset += 4;
            }
        } else {
            codec = new FourCC(data.u32(offset));
            offset += 4;
        }

        List<FLVExAudioTrack> tracks = new LinkedList<>();
        while (offset < data.length()) {
            int audioTrackId = 0;
            int sizeOfAudioTrack;

            if (rawAudioMultitrackType == -1) {
                // Single track.
                sizeOfAudioTrack = data.length() - offset;
            } else {
                if (rawAudioMultitrackType == FLVExAudioMultitrackType.MANY_TRACKS_MANY_CODECS.id) {
                    codec = new FourCC(data.u32(offset));
                    offset += 4;
                }

                audioTrackId = data.u8(offset++);

                if (rawAudioMultitrackType == FLVExAudioMultitrackType.ONE_TRACK.id) {
                    sizeOfAudioTrack = data.length() - offset;
                } else {
                    sizeOfAudioTrack = data.u24(offset);
                    offset += 3;
                }
            }

            FLVExAudioCodecData trackData = null;
            if (rawAudioPacketType == FLVExAudioPacketType.MULTICHANNEL_CONFIG.id) {
                trackData = FLVExAudioChannelConfig.from(data.slice(offset, sizeOfAudioTrack));
            } else {
                trackData = switch (codec.string()) {
                    // TODO
                    default -> new AudioCodecData.Invalid(data.slice(offset, sizeOfAudioTrack));
                };
            }
            offset += sizeOfAudioTrack;

            tracks.add(new FLVExAudioTrack(codec, audioTrackId, trackData));
        }

        return new FLVExAudioTagData(
            data,
            rawAudioPacketType,
            modifiers.toArray(new FLVExAudioModifier[0]),
            tracks.toArray(new FLVExAudioTrack[0])
        );
    }

    @Override
    public final String toString() {
        return String.format(
            "FLVExAudioTagData[type=%s (%d), isSequenceHeader=%b, modifiers=%s, tracks=%s]",
            this.type(), this.rawType,
            this.isSequenceHeader(),
            Arrays.toString(this.modifiers),
            Arrays.toString(this.tracks)
        );
    }

}
