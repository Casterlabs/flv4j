package co.casterlabs.flv4j.codecs.video.av01;

import java.util.Arrays;

import co.casterlabs.flv4j.actionscript.io.ASByteView;
import co.casterlabs.flv4j.codecs.video.av01.AV1OBU.LEB128;
import co.casterlabs.flv4j.documentation.CreatesCopy;

// https://aomediacodec.github.io/av1-spec/#obu-syntax
public record AV1OBUs(ASByteView view) implements AV1PacketData {

    @CreatesCopy
    public AV1OBUs from(AV1OBU[] obus) {
        int requiredLength = 0;
        for (AV1OBU obu : obus) {
            requiredLength += obu.view().length();
        }

        byte[] data = new byte[requiredLength];
        for (int i = 0, offset = 0; i < obus.length; i++) {
            AV1OBU obu = obus[i];
            System.arraycopy(obu.view().buffer(), obu.view().offset(), data, offset, obu.view().length());
            offset += obu.view().length();
        }
        return new AV1OBUs(new ASByteView(data));
    }

    public int numberOfObus() {
        int count = 0;
        int offset = 0;

        while (offset < this.view.length()) {
            int header = this.view.u8(offset++);
            boolean hasExtension = (header & 0b100) != 0;
            boolean hasSizeField = (header & 0b10) != 0;

            if (hasExtension) {
                offset++;
            }

            if (hasSizeField) {
                LEB128 payloadLength = LEB128.parse(this.view, offset);
                offset = Math.toIntExact(offset + payloadLength.value() + payloadLength.byteWidth());
            } else {
                // A sizeless OBU consumes the remainder of the buffer.
                return count + 1;
            }

            count++;
        }

        return count;
    }

    public AV1OBU[] obus() {
        AV1OBU[] obus = new AV1OBU[this.numberOfObus()];
        int offset = 0;
        int idx = 0;
        while (offset < this.view.length()) {
            AV1OBU obu = new AV1OBU(this.view.slice(offset));
            obus[idx++] = obu;
            offset += obu.view().length();
        }
        return obus;
    }

    @Override
    public String toString() {
        return Arrays.toString(this.obus());
    }

}
