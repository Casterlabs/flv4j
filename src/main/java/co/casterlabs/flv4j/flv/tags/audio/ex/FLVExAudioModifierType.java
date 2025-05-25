package co.casterlabs.flv4j.flv.tags.audio.ex;

import lombok.AllArgsConstructor;

// https://veovera.org/docs/enhanced/enhanced-rtmp-v2#enhanced-audio
@AllArgsConstructor
public enum FLVExAudioModifierType {
    /**
     * This block processes TimestampOffsetNano to enhance RTMP timescale accuracy
     * and compatibility with formats like MP4, M2TS, and Safari's Media Source
     * Extensions. It ensures precise synchronization without altering core RTMP
     * timestamps, applying only to the current media message. These adjustments
     * enhance synchronization and timing accuracy in media messages while
     * preserving the core RTMP timestamp integrity.
     * 
     * @apiNote - 1 millisecond (ms) = 1,000,000 nanoseconds (ns). - Maximum value
     *          representable with 20 bits is 1,048,575 ns (just over 1 ms),
     *          allowing precise sub-millisecond adjustments.
     */
    TIMESTAMP_OFFSET_NANO(0),
    ;

    public static final FLVExAudioModifierType[] LUT = new FLVExAudioModifierType[16];
    static {
        for (FLVExAudioModifierType e : values()) {
            LUT[e.id] = e;
        }
    }

    public final int id;

}
