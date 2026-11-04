package co.casterlabs.flv4j.codecs.audio.flac;

import co.casterlabs.flv4j.actionscript.io.ASBitReader;
import co.casterlabs.flv4j.actionscript.io.ASByteView;

// https://www.rfc-editor.org/info/rfc9639/#section-9.1
public record FLACFrameHeader(
    int blockSizeCode,
    int blockSize,
    int sampleRateCode,
    int sampleRate,
    int channelMode,
    int channels,
    int bitsPerSampleCode,
    int bitsPerSample,
    int frameNumber,
    boolean variableSize,
    int reservedZeroBit,
    int headerLength,
    int headerCrc
) {

    // @formatter:off
    private static final int[] BLOCK_SIZE_TABLE = {
            0,
            192,
            576,
            576 << 1,
            576 << 2,
            576 << 3,
            0,
            0,
            256,
            256 << 1,
            256 << 2,
            256 << 3,
            256 << 4,
            256 << 5,
            256 << 6,
            256 << 7
    };

    private static final int[] SAMPLE_RATE_TABLE = {
            0, 88200, 176400, 192000, 8000, 16000, 22050,  
            24000, 32000, 44100, 48000, 96000, 0, 0, 0, 0
    };

    private static final int[] SAMPLE_SIZE_TABLE = {
            0, 8, 12, 0, 16, 20, 24, 32
    };
    // @formatter:on

    public static FLACFrameHeader from(ASByteView view) {
        ASBitReader b = new ASBitReader(view);

        // stream_marker (14) + is_last_frame (1), read as one 15-bit value.
        long sync15 = b.u(15);
        if ((sync15 & 0x7FFF) != 0x7FFC) {
            throw new IllegalArgumentException("FLAC frame sync code mismatch: expected 0x7FFC, got 0x" + Integer.toHexString((int) (sync15 & 0x7FFF)));
        }
        boolean variableSize = b.bit() == 1;

        int blockSizeCode = (int) b.u(4);
        int sampleRateCode = (int) b.u(4);
        int channelMode = (int) b.u(4);
        int channels;
        if (channelMode < 8) {
            channels = channelMode + 1;
        } else if (channelMode < 11) {
            channels = 2;
        } else {
            throw new IllegalArgumentException("Invalid FLAC channel mode: " + channelMode);
        }

        int bitsPerSampleCode = (int) b.u(3);
        if (bitsPerSampleCode == 3) {
            throw new IllegalArgumentException("Invalid FLAC bits-per-sample code: 3");
        }
        int bitsPerSample = SAMPLE_SIZE_TABLE[bitsPerSampleCode];

        int reservedZeroBit = b.bit();
        int frameNumber = parseFrameNumber(b);

        int blockSize;
        if (blockSizeCode == 6) {
            blockSize = (int) b.u(8) + 1;
        } else if (blockSizeCode == 7) {
            blockSize = (int) b.u(16) + 1;
        } else {
            blockSize = BLOCK_SIZE_TABLE[blockSizeCode];
        }

        int sampleRate;
        if (sampleRateCode < 12) {
            sampleRate = SAMPLE_RATE_TABLE[sampleRateCode];
        } else if (sampleRateCode == 12) {
            sampleRate = (int) b.u(8) * 1000;
        } else if (sampleRateCode == 13) {
            sampleRate = (int) b.u(16);
        } else if (sampleRateCode == 14) {
            sampleRate = (int) b.u(16) * 10;
        } else {
            throw new IllegalArgumentException("Invalid FLAC sample rate code: " + sampleRateCode);
        }

        int headerCrc = (int) b.u(8);
        int headerLength = b.bitsRead() / 8;

        return new FLACFrameHeader(
            blockSizeCode, blockSize,
            sampleRateCode, sampleRate,
            channelMode, channels,
            bitsPerSampleCode, bitsPerSample,
            frameNumber, variableSize,
            reservedZeroBit, headerLength, headerCrc
        );
    }

    public String channelModeName() {
        return switch (this.channelMode) {
            case 0, 1, 2, 3, 4, 5, 6, 7 -> "independent";
            case 8 -> "left-side";
            case 9 -> "right-side";
            case 10 -> "mid-side";
            default -> "code-" + this.channelMode;
        };
    }

    @Override
    public String toString() {
        return String.format(
            "FlacFrameHeader{blockSize=%d (code %d), sampleRate=%d (code %d), channelMode=%d (%s), channels=%d, bitsPerSample=%d (code %d), frameNumber=%d, variableSize=%b, headerLength=%dB, headerCrc=0x%02X}",
            this.blockSize, this.blockSizeCode,
            this.sampleRate, this.sampleRateCode,
            this.channelMode, this.channelModeName(),
            this.channels,
            this.bitsPerSample, this.bitsPerSampleCode,
            this.frameNumber, this.variableSize,
            this.headerLength, this.headerCrc
        );
    }

    // UTF-8-encoded unsigned integer:
    // 0xxxxxxx
    // 110xxxxx 10xxxxxx
    // 1110xxxx 10xxxxxx 10xxxxxx
    // 11110xxx and above - invalid/unsupported
    private static int parseFrameNumber(ASBitReader b) {
        int value = (int) b.u(8);
        if ((value & 0x80) == 0) {
            return value; // 1 byte
        }
        if ((value & 0xE0) == 0xC0) {
            return ((value & 0x1F) << 6) | ((int) b.u(8) & 0x3F); // 2 bytes
        }
        if ((value & 0xF0) == 0xE0) {
            return ((value & 0x0F) << 12)
                | (((int) b.u(8) & 0x3F) << 6)
                | ((int) b.u(8) & 0x3F); // 3 bytes
        }
        throw new IllegalArgumentException("Invalid FLAC frame number byte: 0x" + Integer.toHexString(value));
    }

}
