package co.casterlabs.flv4j.codecs.video.avc1;

import co.casterlabs.flv4j.actionscript.io.ASByteView;
import co.casterlabs.flv4j.documentation.CreatesCopy;

public record AVCNalu(ASByteView view) {

    public static AVCNalu from(byte[] data) {
        return new AVCNalu(new ASByteView(data));
    }

    public int fbz() {
        return (this.view.u8(0) >> 7) & 0b1;
    }

    public int nalRefIdc() {
        return (this.view.u8(0) >> 5) & 0b11;
    }

    public boolean discardable() {
        return this.nalRefIdc() == 0;
    }

    public int rawType() {
        return this.view.u8(0) & 0b11111;
    }

    public AVCNaluType type() {
        return AVCNaluType.LUT[this.rawType()];
    }

    @CreatesCopy
    public ASByteView rbsp() {
        byte[] out = new byte[this.view.length()];
        int written = 0;
        int zeros = 0;
        for (int i = 1; i < this.view.length(); i++) { // Start at 1 to skip the NAL header.
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
            "AVCNalu[fbz=%d, nalRefIdc=%s (%d), type=%s (%d), size=%d]",
            this.fbz(),
            this.discardable() ? "DISCARDABLE" : "IMPORTANT", this.nalRefIdc(),
            this.type(), this.rawType(),
            this.view.length()
        );
    }

}
