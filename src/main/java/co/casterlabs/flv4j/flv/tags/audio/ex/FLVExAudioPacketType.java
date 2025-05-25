package co.casterlabs.flv4j.flv.tags.audio.ex;

import lombok.AllArgsConstructor;

// https://veovera.org/docs/enhanced/enhanced-rtmp-v2#enhanced-audio
@AllArgsConstructor
public enum FLVExAudioPacketType {
    SEQUENCE_START(0),
    CODED_FRAMES(1),

    /**
     * RTMP includes a previously undocumented "audio silence" message. This silence
     * message is identified when an audio message contains a zero-length payload,
     * or more precisely, an empty audio message without an AudioTagHeader,
     * indicating a period of silence. The action to take after receiving a silence
     * message is system dependent. The semantics of the silence message in the
     * Flash Media playback and timing model are as follows:
     * 
     * <ul>
     * <li>Ensure all buffered audio data is played out before entering the silence
     * period: Make sure that any audio data currently in the buffer is fully
     * processed and played. This ensures a clean transition into the silence period
     * without cutting off any audio.</li>
     * 
     * <li>After playing all buffered audio data, flush the audio decoder: Clear the
     * audio decoder to reset its state and prepare it for new input after the
     * silence period.</li>
     * 
     * <li>During the silence period, the audio clock can't be used as the master
     * clock for synchronizing playback: Switch to using the system's wall-clock
     * time to maintain the correct timing for video and other data streams.</li>
     * 
     * <li>Don't wait for audio frames for synchronized A+V playback: Normally,
     * audio frames drive the synchronization of audio and video (A/V) playback.
     * During the silence period, playback should not stall waiting for audio
     * frames. Video and other data streams should continue to play based on the
     * wall-clock time, ensuring smooth playback without audio.</li>
     * </ul>
     * 
     * {@link #SEQUENCE_END} is to have no less than the same meaning as a silence
     * message. While it may seem redundant, we need to introduce this enum to
     * ensure we can signal the end of the audio sequence for any audio track.
     */
    SEQUENCE_END(2),
    MULTICHANNEL_CONFIG(4),
    MULTITRACK(5),
    /**
     * ModEx is a special signal within the AudioPacketType enum that serves to both
     * modify and extend the behavior of the current packet. When this signal is
     * encountered, it indicates the presence of additional modifiers or extensions,
     * requiring further processing to adjust or augment the packet's functionality.
     * ModEx can be used to introduce new capabilities or modify existing ones, such
     * as enabling support for high-precision timestamps or other advanced features
     * that enhance the base packet structure.
     */
    MOD_EX(7),
    ;

    public static final FLVExAudioPacketType[] LUT = new FLVExAudioPacketType[16];
    static {
        for (FLVExAudioPacketType e : values()) {
            LUT[e.id] = e;
        }
    }

    public final int id;

}
