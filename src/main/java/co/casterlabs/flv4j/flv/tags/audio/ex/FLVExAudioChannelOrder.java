package co.casterlabs.flv4j.flv.tags.audio.ex;

import lombok.AllArgsConstructor;

// https://veovera.org/docs/enhanced/enhanced-rtmp-v2#enhanced-audio
@AllArgsConstructor
public enum FLVExAudioChannelOrder {
    /**
     * Only the channel count is specified, without any further information about
     * the channel order.
     */
    UNSPECIFIED(0),
    /**
     * The native channel order (i.e., the channels are in the same order in which
     * as defined in the AudioChannel enum).
     */
    NATIVE(1),
    /**
     * The channel order does not correspond to any predefined order and is stored
     * as an explicit map.
     */
    CUSTOM(2),
    ;

    public static final FLVExAudioChannelOrder[] LUT = new FLVExAudioChannelOrder[16];
    static {
        for (FLVExAudioChannelOrder e : values()) {
            LUT[e.id] = e;
        }
    }

    public final int id;

}
