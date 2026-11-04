package co.casterlabs.flv4j.codecs.audio.aac;

import co.casterlabs.flv4j.actionscript.io.ASByteView;

// https://ossrs.io/lts/zh-cn/assets/files/ISO_IEC_14496-3-AAC-2001-7f4d0b3622b322cb72c78f85d91c449f.pdf#page=33
public record AACAudioSpecificConfig(ASByteView view) implements AACPacketData {

    // @formatter:off
    private static final int[] SAMPLE_RATES = {
        96000, 88200, 64000, 48000, 44100, 32000, 24000, 22050, 16000, 12000, 11025, 8000, 7350, 6000, 5000
    };
    // @formatter:on

    public int audioObjectType() {
        return this.view.u8(0) >> 3;
    }

    public int sampleRateIndex() {
        return ((this.view.u8(0) & 0x07) << 1) | (this.view.u8(1) >> 7);
    }

    /**
     * 
     * @return true when the 24-bit samplingFrequency field is present (index 0xf)
     *         and this view is long enough to carry it.
     */
    public boolean hasExplicitSampleRate() {
        return this.sampleRateIndex() == 0x0F && this.view.length() >= 5;
    }

    /**
     * @return 0 if the index is out of table range, or if it is explicit but the
     *         view is too short.
     */
    public int sampleRate() {
        if (this.hasExplicitSampleRate()) {
            // The field is the frequency shifted right by 3 bits.
            int coded = ((this.view.u8(1) & 0x7F) << 17)
                | (this.view.u8(2) << 9)
                | (this.view.u8(3) << 1)
                | (this.view.u8(4) >> 7);
            return coded << 3;
        }

        int index = this.sampleRateIndex();
        if (index >= SAMPLE_RATES.length) {
            return 0;
        }
        return SAMPLE_RATES[index];
    }

    /**
     * 
     * @return The byte holding the channelConfiguration + the three flags --
     *         shifted 3 bytes past the 24-bit samplingFrequency field when the
     *         index is 0xf.
     */
    private int trailingByte() {
        return this.hasExplicitSampleRate() ? 4 : 1;
    }

    public int channelConfig() {
        return (this.view.u8(this.trailingByte()) >> 3) & 0x0F;
    }

    public int channelCount() {
        // https://ossrs.io/lts/zh-cn/assets/files/ISO_IEC_14496-3-AAC-2001-7f4d0b3622b322cb72c78f85d91c449f.pdf#page=36
        return switch (this.channelConfig()) {
            case 1 -> 1;
            case 2 -> 2;
            case 3 -> 3;
            case 4 -> 4;
            case 5 -> 5;
            case 6 -> 6;
            case 7 -> 8;
            case 8, 9, 10, 11, 12, 13, 14, 15 -> 0; // reserved
            default -> 0;
        };
    }

    public boolean frameLengthFlag() {
        return (this.view.u8(this.trailingByte()) & 0x04) != 0;
    }

    public boolean dependsOnCoreCoder() {
        return (this.view.u8(this.trailingByte()) & 0x02) != 0;
    }

    public boolean extensionFlag() {
        return (this.view.u8(this.trailingByte()) & 0x01) != 0;
    }

    // TODO There's some additional fields that we *could* parse out.

    @Override
    public String toString() {
        return String.format(
            "AACAudioSpecificConfig{audioObjectType=%d, sampleRateIndex=%d, sampleRate=%d, channelConfig=%d, channelCount=%d, frameLengthFlag=%b, dependsOnCoreCoder=%b, extensionFlag=%b}",
            this.audioObjectType(),
            this.sampleRateIndex(),
            this.sampleRate(),
            this.channelConfig(),
            this.channelCount(),
            this.frameLengthFlag(),
            this.dependsOnCoreCoder(),
            this.extensionFlag()
        );
    }

}
