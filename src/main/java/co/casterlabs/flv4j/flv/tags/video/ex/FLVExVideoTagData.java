package co.casterlabs.flv4j.flv.tags.video.ex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import co.casterlabs.flv4j.FourCC;
import co.casterlabs.flv4j.actionscript.io.ASByteView;
import co.casterlabs.flv4j.actionscript.io.ASWriter;
import co.casterlabs.flv4j.codecs.VideoCodecData;
import co.casterlabs.flv4j.flv.tags.video.FLVVideoTagData;
import lombok.NonNull;
import lombok.SneakyThrows;

// https://veovera.org/docs/enhanced/enhanced-rtmp-v2#enhanced-video
public record FLVExVideoTagData(
    ASByteView view, // Passthrough ;)
    int rawFrameType,
    int rawType,
    FLVExVideoModifier[] modifiers,
    FLVExVideoTrack[] tracks
) implements FLVVideoTagData {

    public FLVExVideoFrameType frameType() {
        return FLVExVideoFrameType.LUT[this.rawFrameType];
    }

    public FLVExVideoPacketType type() {
        return FLVExVideoPacketType.LUT[this.rawType];
    }

    @Override
    public boolean isEx() {
        return true;
    }

    @Override
    public boolean isSequenceHeader() {
        return this.rawType == FLVExVideoPacketType.SEQUENCE_START.id;
    }

    public static FLVExVideoTagData from(
        @NonNull FLVExVideoFrameType frameType,
        @NonNull FLVExVideoPacketType type,
        @NonNull FLVExVideoModifier[] modifiers,
        @NonNull FLVExVideoTrack... tracks
    ) {
        return from(frameType.id, type.id, modifiers, tracks);
    }

    @SneakyThrows
    public static FLVExVideoTagData from(
        int rawFrameType,
        int rawType,
        @NonNull FLVExVideoModifier[] modifiers,
        @NonNull FLVExVideoTrack... tracks
    ) {
        assert tracks.length > 0 : "At least one track must be provided.";

        // We have to serialize the content first.
        // That way we have valid data to pass into the constructor.

        int rawMultitrackType = -1; // -1 if not multitrack
        if (tracks.length == 1) {
            // Track 0 is defined by the spec as the "main" track.
            // If it's not in here, then we can assume that Track 0 is using a Standard tag.
            // So, we have to use a "multitrack" signal, even though there's only one track.
            // Weird spec design I guess. But it works so we have to play along.
            boolean hasTrack0 = tracks[0].id() == 0;
            if (!hasTrack0) {
                rawMultitrackType = FLVExVideoMultitrackType.ONE_TRACK.id;
            }
        } else {
            rawMultitrackType = FLVExVideoMultitrackType.MANY_TRACKS.id;

            FourCC firstCodec = tracks[0].codec();
            for (FLVExVideoTrack track : tracks) {
                if (track.codec().bits() != firstCodec.bits()) {
                    rawMultitrackType = FLVExVideoMultitrackType.MANY_TRACKS_MANY_CODECS.id;
                    break;
                }
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ASWriter writer = new ASWriter(baos);

        /*
         * ExVideoTagHeader:
         *
         * bit 7     = isExVideoHeader
         * bits 6-4  = VideoFrameType
         * bits 3-0  = VideoPacketType
         */
        if (modifiers.length == 0) {
            if (rawMultitrackType == -1) {
                writer.u8(1 << 7 | rawFrameType << 4 | rawType);
            } else {
                writer.u8(1 << 7 | rawFrameType << 4 | FLVExVideoPacketType.MULTITRACK.id);
                writer.u8(rawMultitrackType << 4 | rawType);
            }
        } else {
            writer.u8(1 << 7 | rawFrameType << 4 | FLVExVideoPacketType.MOD_EX.id);

            for (int i = 0; i < modifiers.length; i++) {
                FLVExVideoModifier mod = modifiers[i];
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
                        writer.u8(mod.rawType() << 4 | FLVExVideoPacketType.MULTITRACK.id);
                        writer.u8(rawMultitrackType << 4 | rawType);
                    }
                } else {
                    writer.u8(mod.rawType() << 4 | FLVExVideoPacketType.MOD_EX.id);
                }
            }
        }

        // The FOURCC is written once for a normal track, and once for the entire
        // multitrack group when all tracks use the same codec.
        if (rawMultitrackType != FLVExVideoMultitrackType.MANY_TRACKS_MANY_CODECS.id) {
            writer.u32(tracks[0].codec().bits());
        }

        for (FLVExVideoTrack track : tracks) {
            if (rawMultitrackType != -1) {
                if (rawMultitrackType == FLVExVideoMultitrackType.MANY_TRACKS_MANY_CODECS.id) {
                    writer.u32(track.codec().bits());
                }

                writer.u8(track.id());

                if (rawMultitrackType != FLVExVideoMultitrackType.ONE_TRACK.id) {
                    int trackSize = track.data().view().length();

                    // CodedFrames for AVC/HEVC/VVC contains the SI24 composition time offset as
                    // part of the track body.
                    if (rawType == FLVExVideoPacketType.CODED_FRAMES.id &&
                        requiresCompositionTimeOffset(track.codec())) {
                        trackSize += 3;
                    }

                    writer.u24(trackSize);
                }
            }

            // The composition time offset is part of the coded-frame body for AVC, HEVC and
            // VVC. CodedFramesX explicitly omits it and implies zero.
            if (rawType == FLVExVideoPacketType.CODED_FRAMES.id &&
                requiresCompositionTimeOffset(track.codec())) {
                writer.s24(track.compositionTimeOffset());
            }

            writer.bytes(track.data().view());
        }

        return new FLVExVideoTagData(
            new ASByteView(baos.toByteArray()),
            rawFrameType,
            rawType,
            modifiers,
            tracks
        );
    }

    public static FLVExVideoTagData parse(ASByteView data) throws IOException {
        int offset = 0;
        int header = data.u8(offset++);

        /*
         * ExVideoTagHeader:
         *
         * bit 7     = isExVideoHeader
         * bits 6-4  = VideoFrameType
         * bits 3-0  = VideoPacketType
         */
        int rawVideoFrameType = (header >> 4) & 0b111;
        int rawVideoPacketType = header & 0b1111;

        // ModEx packets form a chain which eventually terminates in the actual type.
        List<FLVExVideoModifier> modifiers = new LinkedList<>();
        while (rawVideoPacketType == FLVExVideoPacketType.MOD_EX.id) {
            int modDataSize = data.u8(offset++) + 1;

            if (modDataSize == 256) {
                modDataSize += data.u16(offset);
                offset += 2;
            }

            byte[] modExData = data.bytes(offset, modDataSize);
            offset += modDataSize;

            int modType = (data.u8(offset) >> 4) & 0b1111;
            rawVideoPacketType = data.u8(offset) & 0b1111;
            offset++;

            modifiers.add(new FLVExVideoModifier(modType, modExData));
        }

        int rawVideoMultitrackType = -1; // -1 if not multitrack
        FourCC codec = null; // Silence the compiler

        if (rawVideoPacketType == FLVExVideoPacketType.MULTITRACK.id) {
            // Multitrack signaling is immediately followed by the actual packet type.
            rawVideoMultitrackType = (data.u8(offset) >> 4) & 0b1111;
            rawVideoPacketType = data.u8(offset) & 0b1111; // NB: MUST not be MULTITRACK
            offset++;

            if (rawVideoMultitrackType != FLVExVideoMultitrackType.MANY_TRACKS_MANY_CODECS.id) {
                codec = new FourCC(data.u32(offset));
                offset += 4;
            }
        } else {
            codec = new FourCC(data.u32(offset));
            offset += 4;
        }

        List<FLVExVideoTrack> tracks = new LinkedList<>();
        while (offset < data.length()) {
            int videoTrackId = 0;
            int sizeOfVideoTrack;

            if (rawVideoMultitrackType == -1) {
                // Single track.
                sizeOfVideoTrack = data.length() - offset;
            } else {
                if (rawVideoMultitrackType == FLVExVideoMultitrackType.MANY_TRACKS_MANY_CODECS.id) {
                    // Every track defines its own codec.
                    codec = new FourCC(data.u32(offset));
                    offset += 4;
                }

                videoTrackId = data.u8(offset++);

                if (rawVideoMultitrackType == FLVExVideoMultitrackType.ONE_TRACK.id) {
                    // Has track id, but no size.
                    sizeOfVideoTrack = data.length() - offset;
                } else {
                    sizeOfVideoTrack = data.u24(offset);
                    offset += 3;
                }
            }

            // CODED_FRAMES includes a CTS field for AVC, HEVC and VVC.
            // CODED_FRAMES_X omits this field and therefore implies zero.
            int compositionTimeOffset = 0;
            if (rawVideoPacketType == FLVExVideoPacketType.CODED_FRAMES.id && requiresCompositionTimeOffset(codec)) {
                compositionTimeOffset = data.s24(offset);
                offset += 3;
                sizeOfVideoTrack -= 3;
            }

            ASByteView trackDataView = data.slice(offset, sizeOfVideoTrack);
            FLVExVideoCodecData trackData;

            trackData = switch (codec.string()) {
                // TODO
                default -> new VideoCodecData.Invalid(trackDataView);
            };
            offset += sizeOfVideoTrack;

            tracks.add(new FLVExVideoTrack(codec, videoTrackId, compositionTimeOffset, trackData));
        }

        return new FLVExVideoTagData(
            data,
            rawVideoFrameType,
            rawVideoPacketType,
            modifiers.toArray(new FLVExVideoModifier[0]),
            tracks.toArray(new FLVExVideoTrack[0])
        );
    }

    private static boolean requiresCompositionTimeOffset(FourCC codec) {
        return switch (codec.string()) {
            case "avc1", "hvc1", "vvc1" -> true;
            default -> false;
        };
    }

    @Override
    public final String toString() {
        return String.format(
            "FLVExVideoTagData[frameType=%s (%d), type=%s (%d), isSequenceHeader=%b, modifiers=%s, tracks=%s]",
            this.frameType(), this.rawFrameType,
            this.type(), this.rawType,
            this.isSequenceHeader(),
            Arrays.toString(this.modifiers),
            Arrays.toString(this.tracks)
        );
    }

}
