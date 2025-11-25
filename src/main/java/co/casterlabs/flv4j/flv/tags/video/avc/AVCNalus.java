package co.casterlabs.flv4j.flv.tags.video.avc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import co.casterlabs.flv4j.actionscript.io.ASByteView;

// This is a helper class to parse NAL units from AVC video data.
// Each NAL is prefixed with a 4-byte length field.
// We have to count how many NALs are present in the given ASByteView.
public record AVCNalus(ASByteView view) implements AVCPacketData {

    public int numberOfNals() {
        int offset = 0;
        int count = 0;
        while (offset + 4 <= this.view.length()) {
            long nalLength = this.view.u32(offset);
            offset += 4 + nalLength;
            count++;
        }

        return count;
    }

    public AVCNalu[] getNalus() {
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
        return Arrays.toString(this.getNalus());
    }

}
