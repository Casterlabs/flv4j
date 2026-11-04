package co.casterlabs.flv4j.codecs.video.hvc1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import co.casterlabs.flv4j.actionscript.io.ASByteView;
import co.casterlabs.flv4j.documentation.CreatesCopy;

// This is a helper class to parse NAL units from HEVC video data.
// This parser currently assumes 4-byte length prefixes.
// We have to count how many NALUs are present in the given ASByteView.
public record HEVCNalus(ASByteView view) implements HEVCPacketData {

    @CreatesCopy
    public static HEVCNalus from(HEVCNalu[] nalus) {
        int requiredLength = nalus.length * 4; // 4 bytes for each NAL length prefix
        for (HEVCNalu nal : nalus) {
            requiredLength += nal.view().length();
        }

        byte[] data = new byte[requiredLength];
        for (int i = 0, offset = 0; i < nalus.length; i++) {
            HEVCNalu nal = nalus[i];
            int nalLength = nal.view().length();

            // Write the length prefix
            data[offset++] = (byte) ((nalLength >> 24) & 0xFF);
            data[offset++] = (byte) ((nalLength >> 16) & 0xFF);
            data[offset++] = (byte) ((nalLength >> 8) & 0xFF);
            data[offset++] = (byte) ((nalLength >> 0) & 0xFF);

            // Write the NALU data
            System.arraycopy(nal.view().buffer(), nal.view().offset(), data, offset, nalLength);
            offset += nalLength;
        }
        return new HEVCNalus(new ASByteView(data));
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

    public HEVCNalu[] nalus() {
        List<HEVCNalu> nals = new ArrayList<>();
        int offset = 0;
        while (offset + 4 <= this.view.length()) {
            long nalLength = this.view.u32(offset);
            offset += 4;
            ASByteView nalView = this.view.slice(offset, (int) nalLength);
            nals.add(new HEVCNalu(nalView));
            offset += nalLength;
        }
        return nals.toArray(new HEVCNalu[0]);
    }

    @Override
    public String toString() {
        return Arrays.toString(this.nalus());
    }

}
