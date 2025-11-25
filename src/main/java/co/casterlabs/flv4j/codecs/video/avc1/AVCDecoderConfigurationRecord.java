package co.casterlabs.flv4j.codecs.video.avc1;

import java.util.Arrays;

import co.casterlabs.flv4j.actionscript.io.ASByteView;

public record AVCDecoderConfigurationRecord(ASByteView view) implements AVCPacketData {

    public int configurationVersion() {
        return this.view.u8(0);
    }

    public int profileIndication() {
        return this.view.u8(1);
    }

    public int profileCompatibility() {
        return this.view.u8(2);
    }

    public int levelIndication() {
        return this.view.u8(3);
    }

    public int lengthSizeMinusOne() {
        return this.view.u8(4) & 0b11;
    }

    public int numOfSPS() {
        return this.view.u8(5) & 0b11111;
    }

    public int numOfPPS() {
        int offset = this._spsSkip();
        return this.view.u8(offset);
    }

    public AVCNalu[] sps() {
        AVCNalu[] sps = new AVCNalu[this.numOfSPS()];
        int offset = 6;
        for (int i = 0; i < sps.length; i++) {
            int spsLength = this.view.u16(offset);
            offset += 2;
            ASByteView spsView = this.view.slice(offset, spsLength);
            sps[i] = new AVCNalu(spsView);
            offset += spsLength;
        }
        return sps;
    }

    public AVCNalu[] pps() {
        AVCNalu[] pps = new AVCNalu[this.numOfPPS()];
        int offset = this._spsSkip();
        offset += 1; // Skip numOfPPS
        for (int i = 0; i < pps.length; i++) {
            int ppsLength = this.view.u16(offset);
            offset += 2;
            ASByteView ppsView = this.view.slice(offset, ppsLength);
            pps[i] = new AVCNalu(ppsView);
            offset += ppsLength;
        }
        return pps;
    }

    @Override
    public String toString() {
        return String.format(
            "AVCDecoderConfigurationRecord[configurationVersion=%d, profileIndication=%d, profileCompatibility=%d, levelIndication=%d, lengthSizeMinusOne=%d, sps=%s, pps=%s]",
            this.configurationVersion(),
            this.profileIndication(),
            this.profileCompatibility(),
            this.levelIndication(),
            this.lengthSizeMinusOne(),
            Arrays.toString(this.sps()),
            Arrays.toString(this.pps())
        );
    }

    private int _spsSkip() {
        int offset = 6;
        for (int i = 0; i < this.numOfSPS(); i++) {
            int spsLength = this.view.u16(offset);
            offset += 2 + spsLength;
        }
        return offset;
    }

}
