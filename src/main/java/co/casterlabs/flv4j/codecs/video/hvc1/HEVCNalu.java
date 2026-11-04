package co.casterlabs.flv4j.codecs.video.hvc1;

import co.casterlabs.flv4j.actionscript.io.ASByteView;
import co.casterlabs.flv4j.documentation.CreatesCopy;

// https://www.itu.int/rec/T-REC-H.265-202601-I/en §7.4.2.2
public record HEVCNalu(ASByteView view) {

    public static HEVCNalu from(byte[] data) {
        return new HEVCNalu(new ASByteView(data));
    }

    public int fbz() {
        return (this.view.u8(0) >> 7) & 0b1;
    }

    public int rawType() {
        return (this.view.u8(0) >> 1) & 0b111111;
    }

    public HEVCNaluType type() {
        return HEVCNaluType.LUT[this.rawType()];
    }

    public int nuhLayerId() {
        return ((this.view.u8(0) & 0b1) << 5) | ((this.view.u8(1) >> 3) & 0b11111);
    }

    public int nuhTemporalIdPlus1() {
        return this.view.u8(1) & 0b111;
    }

    @CreatesCopy
    public ASByteView rbsp() {
        byte[] out = new byte[this.view.length()];
        int written = 0;
        int zeros = 0;
        for (int i = 2; i < this.view.length(); i++) { // Start at 2 to skip the NAL header.
            byte b = this.view.buffer()[i + this.view.offset()];
            if (zeros >= 2 && b == 0x03) {
                zeros = 0; // Drop the emulation_prevention_three_byte
                continue;
            }
            out[written++] = b;
            zeros = (b == 0) ? zeros + 1 : 0;
        }
        return new ASByteView(out, 0, written);
    }

    @Override
    public String toString() {
        return String.format(
            "HEVCNalu[fbz=%d, type=%s (%d), nuhLayerId=%d, nuhTemporalIdPlus1=%d, size=%d]",
            this.fbz(),
            this.type(), this.rawType(),
            this.nuhLayerId(),
            this.nuhTemporalIdPlus1(),
            this.view.length()
        );
    }

}
