package co.casterlabs.flv4j.codecs.video.avc1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import co.casterlabs.flv4j.actionscript.io.ASByteView;

// This is a helper class to parse NAL units from AVC video data.
// Each NAL is prefixed with a 4-byte length field.
// We have to count how many NALs are present in the given ASByteView.
public record AVCNalus(ASByteView view) implements AVCPacketData {

    public AVCNalus from(AVCNalu[] nalus) {
        int requiredLength = nalus.length * 4; // 4 bytes for each NAL length prefix
        for (AVCNalu nal : nalus) {
            requiredLength += nal.view().length();
        }

        byte[] data = new byte[requiredLength];
        for (int i = 0, offset = 0; i < nalus.length; i++) {
            AVCNalu nal = nalus[i];
            int nalLength = nal.view().length();

            // Write the length prefix
            data[offset++] = (byte) ((nalLength >> 24) & 0xFF);
            data[offset++] = (byte) ((nalLength >> 16) & 0xFF);
            data[offset++] = (byte) ((nalLength >> 8) & 0xFF);
            data[offset++] = (byte) ((nalLength >> 0) & 0xFF);

            // Write the NAL data
            System.arraycopy(nal.view().buffer(), nal.view().offset(), data, offset, nalLength);
            offset += nalLength;
        }
        return new AVCNalus(new ASByteView(data));
    }

    public int numberOfNalus() {
        int offset = 0;
        int count = 0;
        while (offset + 4 <= this.view.length()) {
            long nalLength = this.view.u32(offset);
            offset += 4 + nalLength;
            count++;
        }

        return count;
    }

    public AVCNalu[] nalus() {
        List<AVCNalu> nals = new ArrayList<>();
        int offset = 0;
        while (offset + 4 <= this.view.length()) {
            long nalLength = this.view.u32(offset);
            offset += 4;
            ASByteView nalView = this.view.slice(offset, (int) nalLength);
            nals.add(new AVCNalu(nalView));
            offset += nalLength;
        }
        return nals.toArray(new AVCNalu[0]);
    }

    @Override
    public String toString() {
        return Arrays.toString(this.nalus());
    }

}
