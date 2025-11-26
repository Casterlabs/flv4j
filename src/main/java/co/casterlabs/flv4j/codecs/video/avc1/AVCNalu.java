package co.casterlabs.flv4j.codecs.video.avc1;

import co.casterlabs.flv4j.actionscript.io.ASByteView;

public record AVCNalu(ASByteView view) {

    public AVCNalu from(byte[] data) {
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

    public AVCNaluType type() {
        int rawType = this.view.u8(0) & 0b11111;
        return AVCNaluType.LUT[rawType];
    }

    @Override
    public String toString() {
        return String.format(
            "AVCNalu[fbz=%d, nalRefIdc=%s (%d), type=%s (%d), size=%d]",
            this.fbz(),
            this.discardable() ? "DISCARDABLE" : "IMPORTANT", this.nalRefIdc(),
            this.type(), this.view.u8(0) & 0b11111,
            this.view.length()
        );
    }

}
