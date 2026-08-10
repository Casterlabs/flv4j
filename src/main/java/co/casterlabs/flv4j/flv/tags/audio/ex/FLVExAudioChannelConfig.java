package co.casterlabs.flv4j.flv.tags.audio.ex;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.flv4j.actionscript.io.ASAssert;
import co.casterlabs.flv4j.actionscript.io.ASByteView;
import lombok.NonNull;

public record FLVExAudioChannelConfig(ASByteView view, int rawOrder, int channelCount, FLVExAudioChannel[] customOrder, long nativeOrder) implements FLVExAudioCodecData {

    public FLVExAudioChannelConfig(ASByteView view, int rawOrder, int channelCount, FLVExAudioChannel[] customOrder, long nativeOrder) {
        ASAssert.u8(rawOrder, "rawOrder");
        ASAssert.u8(channelCount, "channelCount");

        this.view = view;
        this.rawOrder = rawOrder;
        this.channelCount = channelCount;

        if (rawOrder == FLVExAudioChannelOrder.CUSTOM.id) {
            assert customOrder != null : "customOrder cannot be null when order is CUSTOM";
            this.customOrder = customOrder;
            this.nativeOrder = 0;
        } else if (rawOrder == FLVExAudioChannelOrder.UNSPECIFIED.id) {
            ASAssert.u32(nativeOrder, "nativeOrder");
            this.customOrder = null;
            this.nativeOrder = nativeOrder;
        } else {
            this.customOrder = null;
            this.nativeOrder = 0;
        }
    }

    public static FLVExAudioChannelConfig from(ASByteView data) {
        int rawOrder = data.u8(0);
        int channelCount = data.u8(1);

        return switch (rawOrder) {
            case 0 -> new FLVExAudioChannelConfig(data, 0, channelCount, null, 0);
            case 1 -> {
                long nativeOrder = data.u32(2);
                yield new FLVExAudioChannelConfig(data, 1, channelCount, null, nativeOrder);
            }
            case 2 -> {
                FLVExAudioChannel[] customOrder = new FLVExAudioChannel[channelCount];
                for (int i = 0; i < channelCount; i++) {
                    int channelId = data.u8(2 + i);
                    customOrder[i] = FLVExAudioChannel.LUT[channelId];
                }
                yield new FLVExAudioChannelConfig(data, 2, channelCount, customOrder, 0);
            }
            default -> new FLVExAudioChannelConfig(data, 0, channelCount, null, 0);
        };

    }

    public static FLVExAudioChannelConfig from(int channelCount) {
        byte[] raw = new byte[] {
                (byte) FLVExAudioChannelOrder.UNSPECIFIED.id,
                (byte) channelCount
        };

        return new FLVExAudioChannelConfig(
            new ASByteView(raw),
            FLVExAudioChannelOrder.UNSPECIFIED.id,
            channelCount,
            null,
            0
        );
    }

    public static FLVExAudioChannelConfig from(int channelCount, long nativeOrder) {
        byte[] raw = new byte[] {
                (byte) FLVExAudioChannelOrder.NATIVE.id,
                (byte) channelCount,
                (byte) ((nativeOrder >> 24) & 0xFF),
                (byte) ((nativeOrder >> 16) & 0xFF),
                (byte) ((nativeOrder >> 8) & 0xFF),
                (byte) (nativeOrder & 0xFF),
        };

        return new FLVExAudioChannelConfig(
            new ASByteView(raw),
            FLVExAudioChannelOrder.NATIVE.id,
            channelCount,
            null,
            nativeOrder
        );
    }

    public static FLVExAudioChannelConfig from(@NonNull FLVExAudioChannel[] customOrder) {
        byte[] raw = new byte[2 + customOrder.length];
        raw[0] = (byte) FLVExAudioChannelOrder.CUSTOM.id;
        raw[1] = (byte) customOrder.length;
        for (int i = 0; i < customOrder.length; i++) {
            raw[2 + i] = (byte) customOrder[i].id;
        }

        return new FLVExAudioChannelConfig(
            new ASByteView(raw),
            FLVExAudioChannelOrder.CUSTOM.id,
            customOrder.length,
            customOrder,
            0
        );
    }

    @Override
    public boolean isSequenceHeader() {
        return true;
    }

    public FLVExAudioChannelOrder order() {
        return FLVExAudioChannelOrder.LUT[this.rawOrder];
    }

    /**
     * Each entry specifies the speaker layout (see AudioChannel enum above for
     * layout definition) in the order that it appears in the bitstream. First entry
     * (i.e., index 0) specifies the speaker layout for channel 1. Subsequent
     * entries specify the speaker layout for the next channels (e.g., second entry
     * for channel 2, third entry for channel 3, etc.).
     */
    public @Nullable FLVExAudioChannel[] customOrder() {
        return this.customOrder;
    }

    /**
     * audioChannelFlags indicates which channels are present in the multi-channel
     * stream. You can perform a Bitwise AND (i.e., audioChannelFlags &
     * AudioChannelMask.xxx) to see if a specific audio channel is present
     */
    public long nativeOrder() {
        return this.nativeOrder;
    }

}
