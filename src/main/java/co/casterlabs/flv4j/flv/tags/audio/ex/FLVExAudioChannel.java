package co.casterlabs.flv4j.flv.tags.audio.ex;

import lombok.AllArgsConstructor;

// https://veovera.org/docs/enhanced/enhanced-rtmp-v2#enhanced-audio
@AllArgsConstructor
public enum FLVExAudioChannel {
    // commonly used speaker configurations
    // see - https://en.wikipedia.org/wiki/Surround_sound#Standard_speaker_channels
    FRONT_LEFT(0),
    FRONT_RIGHT(1),
    FRONT_CENTER(2),
    LOW_FREQUENCY1(3),
    BACK_LEFT(4),
    BACK_RIGHT(5),
    FRONT_LEFT_CENTER(6),
    FRONT_RIGHT_CENTER(7),
    BACK_CENTER(8),
    SIDE_LEFT(9),
    SIDE_RIGHT(10),
    TOP_CENTER(11),
    TOP_FRONT_LEFT(12),
    TOP_FRONT_CENTER(13),
    TOP_FRONT_RIGHT(14),
    TOP_BACK_LEFT(15),
    TOP_BACK_CENTER(16),
    TOP_BACK_RIGHT(17),

    // mappings to complete 22.2 multichannel audio, as
    // standardized in SMPTE ST2036-2-2008
    // see - https://en.wikipedia.org/wiki/22.2_surround_sound
    LOW_FREQUENCY2(18),
    TOP_SIDE_LEFT(19),
    TOP_SIDE_RIGHT(20),
    BOTTOM_FRONT_CENTER(21),
    BOTTOM_FRONT_LEFT(22),
    BOTTOM_FRONT_RIGHT(23),

    // 24 - Reserved
    // ...
    // 0xfd - Reserved

    // Channel is empty and can be safely skipped.
    UNUSED(0xfe),

    // Channel contains data, but its speaker configuration is unknown.
    UNKNOWN(0xff),
    ;

    // @formatter:off
    public static final int MASK_FRONT_LEFT           = 0x000001;
    public static final int MASK_FRONT_RIGHT          = 0x000002;
    public static final int MASK_FRONT_CENTER         = 0x000004;
    public static final int MASK_LOW_FREQUENCY1       = 0x000008;
    public static final int MASK_BACK_LEFT            = 0x000010;
    public static final int MASK_BACK_RIGHT           = 0x000020;
    public static final int MASK_FRONT_LEFT_CENTER    = 0x000040;
    public static final int MASK_FRONT_RIGHT_CENTER   = 0x000080;
    public static final int MASK_BACK_CENTER          = 0x000100;
    public static final int MASK_SIDE_LEFT            = 0x000200;
    public static final int MASK_SIDE_RIGHT           = 0x000400;
    public static final int MASK_TOP_CENTER           = 0x000800;
    public static final int MASK_TOP_FRONT_LEFT       = 0x001000;
    public static final int MASK_TOP_FRONT_CENTER     = 0x002000;
    public static final int MASK_TOP_FRONT_RIGHT      = 0x004000;
    public static final int MASK_TOP_BACK_LEFT        = 0x008000;
    public static final int MASK_TOP_BACK_CENTER      = 0x010000;
    public static final int MASK_TOP_BACK_RIGHT       = 0x020000;
    public static final int MASK_LOW_FREQUENCY2       = 0x040000;
    public static final int MASK_TOP_SIDE_LEFT        = 0x080000;
    public static final int MASK_TOP_SIDE_RIGHT       = 0x100000;
    public static final int MASK_BOTTOM_FRONT_CENTER  = 0x200000;
    public static final int MASK_BOTTOM_FRONT_LEFT    = 0x400000;
    public static final int MASK_BOTTOM_FRONT_RIGHT   = 0x800000;
    // @formatter:on

    public static final FLVExAudioChannel[] LUT = new FLVExAudioChannel[256];
    static {
        for (FLVExAudioChannel e : values()) {
            LUT[e.id] = e;
        }
    }

    public final int id;

}
