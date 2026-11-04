package co.casterlabs.flv4j.codecs.audio.mp3;

import co.casterlabs.flv4j.actionscript.io.ASByteView;

// https://teslabs.com/openplayer/docs/docs/specs/mp3_structure2.pdf
public record MP3FrameHeader(ASByteView view) {

    // @formatter:off
    private static final int[] MPEG1_LAYER_III = {0, 32, 40, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320, 0};
    private static final int[] MPEG1_LAYER_II = {0, 32, 48, 56, 64, 80, 96, 112, 128, 144, 160, 176, 192, 224, 256, 0};
    private static final int[] MPEG1_LAYER_I = {0, 32, 64, 96, 128, 160, 192, 224, 256, 288, 320, 352, 384, 416, 448, 0};
    
    private static final int[] MPEG2_LAYER_III = {0, 8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 160, 0};
    private static final int[] MPEG2_LAYER_II = {0, 8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 160, 0};
    private static final int[] MPEG2_LAYER_I = {0, 32, 48, 56, 64, 80, 96, 112, 128, 144, 160, 176, 192, 224, 256, 0};

    private static final int[] MPEG1_SAMPLE_RATES = {44100, 48000, 32000};
    private static final int[] MPEG2_SAMPLE_RATES = {22050, 24000, 16000};
    private static final int[] MPEG25_SAMPLE_RATES = {11025, 12000, 8000};
    // @formatter:on

    public int syncBits() {
        // 11 bits: all 8 bits of byte 0 + the top 3 bits of byte 1.
        return this.view.u8(0) << 3 | (this.view.u8(1) >> 5);
    }

    public boolean sync() {
        return this.syncBits() == 0x7FF;
    }

    public int version() {
        // 3 = MPEG-1, 2 = MPEG-2, 1 = MPEG-2.5, 0 = reserved.
        return (this.view.u8(1) >> 3) & 0x03;
    }

    public int layer() {
        // 3 = Layer I, 2 = Layer II, 1 = Layer III, 0 = reserved.
        return (this.view.u8(1) >> 1) & 0x03;
    }

    public boolean hasCRC() {
        // The protection bit is inverted: 1 = no CRC.
        return (this.view.u8(1) & 0x01) == 0;
    }

    public int bitrateIndex() {
        // 0 = free format, 15 = bad index.
        return (this.view.u8(2) >> 4) & 0x0F;
    }

    public int bitrate() {
        // 0 for free format (index 0), bad index (15), and reserved version/layer.
        int[] table = switch (this.version()) {
            case 3 -> switch (this.layer()) {
                case 1 -> MPEG1_LAYER_III;
                case 2 -> MPEG1_LAYER_II;
                case 3 -> MPEG1_LAYER_I;
                default -> null;
            };
            case 2, 1 -> switch (this.layer()) {
                case 1 -> MPEG2_LAYER_III;
                case 2 -> MPEG2_LAYER_II;
                case 3 -> MPEG2_LAYER_I;
                default -> null;
            };
            default -> null;
        };
        if (table == null) {
            return 0;
        }
        return table[this.bitrateIndex()] * 1000;
    }

    public int sampleRateIndex() {
        // 3 = reserved.
        return (this.view.u8(2) >> 2) & 0x03;
    }

    public int sampleRate() {
        // 0 if the index is reserved (3) or the version is reserved.
        int[] table = switch (this.version()) {
            case 3 -> MPEG1_SAMPLE_RATES;
            case 2 -> MPEG2_SAMPLE_RATES;
            case 1 -> MPEG25_SAMPLE_RATES;
            default -> null;
        };
        if (table == null) {
            return 0;
        }
        int index = this.sampleRateIndex();
        if (index >= table.length) {
            return 0;
        }
        return table[index];
    }

    public boolean padding() {
        return (this.view.u8(2) & 0x02) != 0;
    }

    public boolean privateBit() {
        return (this.view.u8(2) & 0x01) != 0;
    }

    public int channelMode() {
        // 0 = stereo, 1 = joint stereo, 2 = dual channel, 3 = single channel (mono).
        return (this.view.u8(3) >> 6) & 0x03;
    }

    public int modeExtension() {
        return (this.view.u8(3) >> 4) & 0x03;
    }

    public boolean copyright() {
        return (this.view.u8(3) & 0x08) != 0;
    }

    public boolean original() {
        return (this.view.u8(3) & 0x04) != 0;
    }

    public int emphasis() {
        return this.view.u8(3) & 0x03;
    }

    public int frameSize() {
        // Layer I: 384 samples per frame, 4-byte slots.
        // Layer II: 1152 samples per frame.
        // Layer III: 1152 samples (MPEG-1), 576 (MPEG-2/2.5).
        // Padding adds one slot (4 bytes for Layer I, 1 otherwise).
        if (!this.sync() || this.bitrate() == 0 || this.sampleRate() == 0) {
            return 0;
        }

        if (this.layer() == 3) {
            return (12 * this.bitrate() / this.sampleRate()
                + (this.padding() ? 1 : 0)) * 4;
        }

        int samplesPerFrame = this.layer() == 1 && this.version() != 3
            ? 576
            : 1152;

        return samplesPerFrame * this.bitrate()
            / this.sampleRate() / 8
            + (this.padding() ? 1 : 0);
    }

    @Override
    public String toString() {
        return String.format(
            "MP3FrameHeader{sync=%b, version=%d, layer=%d, hasCRC=%b, bitrateIndex=%d, bitrate=%d, sampleRateIndex=%d, sampleRate=%d, padding=%b, privateBit=%b, channelMode=%d, modeExtension=%d, copyright=%b, original=%b, emphasis=%d, frameSize=%dB}",
            this.sync(),
            this.version(),
            this.layer(),
            this.hasCRC(),
            this.bitrateIndex(),
            this.bitrate(),
            this.sampleRateIndex(),
            this.sampleRate(),
            this.padding(),
            this.privateBit(),
            this.channelMode(),
            this.modeExtension(),
            this.copyright(),
            this.original(),
            this.emphasis(),
            this.frameSize()
        );
    }

}
