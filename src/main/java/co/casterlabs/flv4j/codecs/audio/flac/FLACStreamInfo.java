package co.casterlabs.flv4j.codecs.audio.flac;

import co.casterlabs.flv4j.actionscript.io.ASByteView;

// https://www.rfc-editor.org/info/rfc9639/#section-8.2
public record FLACStreamInfo(ASByteView view) implements FLACPacketData {

    public int minBlocksize() {
        return this.view.u16(0);
    }

    public int maxBlocksize() {
        return this.view.u16(2);
    }

    public int minFrameSize() {
        return this.view.u24(4);
    }

    public int maxFrameSize() {
        return this.view.u24(7);
    }

    public int sampleRate() {
        // 20 bits: byte 10 + byte 11 + top 4 of byte 12.
        return (this.view.u8(10) << 12)
            | (this.view.u8(11) << 4)
            | (this.view.u8(12) >> 4);
    }

    public int channels() {
        // (value - 1) per the spec.
        return ((this.view.u8(12) >> 1) & 0b111) + 1;
    }

    public int bitsPerSample() {
        // (value - 1) per the spec.
        // 5 bits: bottom bit of byte 12 + top 4 of byte 13.
        return ((((this.view.u8(12) & 0b1) << 4)
            | (this.view.u8(13) >> 4))) + 1;
    }

    public long totalSamples() {
        // 36 bits: bottom 4 of byte 13 + bytes 14-17.
        return ((long) (this.view.u8(13) & 0b1111) << 32)
            | ((long) this.view.u8(14) << 24)
            | ((long) this.view.u8(15) << 16)
            | ((long) this.view.u8(16) << 8)
            | this.view.u8(17);
    }

    public byte[] md5() {
        return this.view.bytes(18, 16);
    }

    @Override
    public String toString() {
        return String.format(
            "FlacStreamInfo{minBlocksize=%d, maxBlocksize=%d, minFrameSize=%d, maxFrameSize=%d, sampleRate=%d, channels=%d, bitsPerSample=%d, totalSamples=%d}",
            this.minBlocksize(),
            this.maxBlocksize(),
            this.minFrameSize(),
            this.maxFrameSize(),
            this.sampleRate(),
            this.channels(),
            this.bitsPerSample(),
            this.totalSamples()
        );
    }

}
