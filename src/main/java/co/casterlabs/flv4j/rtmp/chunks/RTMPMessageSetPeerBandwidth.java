package co.casterlabs.flv4j.rtmp.chunks;

import java.io.IOException;

import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASSizer;
import co.casterlabs.flv4j.actionscript.io.ASWriter;
import lombok.AllArgsConstructor;

// https://rtmp.veriskope.com/pdf/rtmp_specification_1.0.pdf#page=19
/**
 * The client or the server sends this message to limit the output bandwidth of
 * its peer. The peer receiving this message limits its output bandwidth by
 * limiting the amount of sent but unacknowledged data to the window size
 * indicated in this message. The peer receiving this message SHOULD respond
 * with {@link RTMPMessageWindowAcknowledgementSize} if the window size is
 * different from the last one sent to the sender of this message.
 */
public record RTMPMessageSetPeerBandwidth(long windowSize, int rawLimitType) implements RTMPMessage {
    private static final int SIZE = new ASSizer().u32().u8().size;

    @Override
    public boolean isControl() {
        return true;
    }

    @Override
    public int rawType() {
        return 6;
    }

    public LimitType limitType() {
        return LimitType.LUT[this.rawLimitType];
    }

    @Override
    public int size() {
        return SIZE;
    }

    @Override
    public void serialize(ASWriter writer) throws IOException {
        writer.u32(this.windowSize);
        writer.u8(this.rawLimitType);
    }

    public static RTMPMessageSetPeerBandwidth parse(ASReader reader) throws IOException {
        long windowSize = reader.u32();
        int rawLimitType = reader.u8();
        return new RTMPMessageSetPeerBandwidth(windowSize, rawLimitType);
    }

    @AllArgsConstructor
    public static enum LimitType {
        /**
         * The peer SHOULD limit its output bandwidth to the indicated window size.
         */
        HARD(0),
        /**
         * The peer SHOULD limit its output bandwidth to the the window indicated in
         * this message or the limit already in effect, whichever is smaller.
         */
        SOFT(1),
        /**
         * If the previous Limit Type was Hard, treat this message as though it was
         * marked Hard, otherwise ignore this message.
         */
        DYNAMIC(2),
        ;

        public static final LimitType[] LUT = new LimitType[3];
        static {
            for (LimitType e : values()) {
                LUT[e.id] = e;
            }
        }

        public final int id;

    }

}
