package co.casterlabs.flv4j.codecs.audio.opus;

import co.casterlabs.flv4j.actionscript.io.ASByteView;

// https://www.rfc-editor.org/info/rfc7845/#section-5.1
//
// NB: OpusHead fields are Little-Endian. Our reader is Big-Endian.
public record OpusHead(ASByteView view) implements OpusPacketData {

    // @formatter:off
    private static final byte[] MAGIC = {'O', 'p', 'u', 's', 'H', 'e', 'a', 'd'};
    // @formatter:on

    public boolean hasMagic() {
        for (int i = 0; i < MAGIC.length; i++) {
            if (this.view.u8(i) != (MAGIC[i] & 0xFF)) {
                return false;
            }
        }
        return true;
    }

    public int version() {
        return this.view.u8(8); // 1 per the spec
    }

    public int channelCount() {
        return this.view.u8(9);
    }

    public int preSkip() {
        return this.view.u8(10) | (this.view.u8(11) << 8);
    }

    public int inputSampleRate() {
        return (int) this.view.u32le(12);
    }

    public int outputGain() {
        return (short) (this.view.u8(16) | (this.view.u8(17) << 8));
    }

    public int channelMappingFamily() {
        // 0 = native (mono/stereo), 1 = Audio Channel Mapping Table family.
        return this.view.u8(18);
    }

    // TODO Channel Mapping Table.

    @Override
    public String toString() {
        return String.format(
            "OpusHead{magic=%s, version=%d, channelCount=%d, preSkip=%d, inputSampleRate=%d, outputGain=%d, channelMappingFamily=%d}",
            this.hasMagic() ? "OpusHead" : "<invalid>",
            this.version(),
            this.channelCount(),
            this.preSkip(),
            this.inputSampleRate(),
            this.outputGain(),
            this.channelMappingFamily()
        );
    }

}
