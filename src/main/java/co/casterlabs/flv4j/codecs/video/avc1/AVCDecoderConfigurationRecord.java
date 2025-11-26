package co.casterlabs.flv4j.codecs.video.avc1;

import java.util.Arrays;

import co.casterlabs.flv4j.actionscript.io.ASAssert;
import co.casterlabs.flv4j.actionscript.io.ASByteView;

public record AVCDecoderConfigurationRecord(ASByteView view) implements AVCPacketData {

    public AVCDecoderConfigurationRecord from(int configurationVersion, int profileIndication, int profileCompatibility, int levelIndication, int lengthSizeMinusOne, AVCNalu[] sps, AVCNalu[] pps) {
        ASAssert.u8(configurationVersion, "configurationVersion");
        ASAssert.u8(profileIndication, "profileIndication");
        ASAssert.u8(profileCompatibility, "profileCompatibility");
        ASAssert.u8(levelIndication, "levelIndication");
        ASAssert.u2(lengthSizeMinusOne, "lengthSizeMinusOne");
        ASAssert.u5(sps.length, "sps.length");
        ASAssert.u8(pps.length, "pps.length");

        int requiredLength = 6; // Fixed header size
        for (AVCNalu nal : sps) {
            requiredLength += 2 + nal.view().length(); // 2 bytes for length prefix
        }
        for (AVCNalu nal : pps) {
            requiredLength += 2 + nal.view().length(); // 2 bytes for length prefix
        }

        byte[] data = new byte[requiredLength];
        int offset = 0;

        data[offset++] = (byte) (configurationVersion & 0xFF);
        data[offset++] = (byte) (profileIndication & 0xFF);
        data[offset++] = (byte) (profileCompatibility & 0xFF);
        data[offset++] = (byte) (levelIndication & 0xFF);
        data[offset++] = (byte) (0b11111100 | (lengthSizeMinusOne & 0b11));
        data[offset++] = (byte) (0b11100000 | (sps.length & 0b11111));

        for (AVCNalu nal : sps) {
            int nalLength = nal.view().length();
            data[offset++] = (byte) ((nalLength >> 8) & 0xFF);
            data[offset++] = (byte) ((nalLength >> 0) & 0xFF);
            System.arraycopy(nal.view().buffer(), nal.view().offset(), data, offset, nalLength);
            offset += nalLength;
        }

        data[offset++] = (byte) (pps.length & 0xFF);

        for (AVCNalu nal : pps) {
            int nalLength = nal.view().length();
            data[offset++] = (byte) ((nalLength >> 8) & 0xFF);
            data[offset++] = (byte) ((nalLength >> 0) & 0xFF);
            System.arraycopy(nal.view().buffer(), nal.view().offset(), data, offset, nalLength);
            offset += nalLength;
        }

        return new AVCDecoderConfigurationRecord(new ASByteView(data));
    }

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
