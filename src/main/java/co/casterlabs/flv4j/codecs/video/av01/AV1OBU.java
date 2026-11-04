package co.casterlabs.flv4j.codecs.video.av01;

import co.casterlabs.flv4j.actionscript.io.ASByteView;

// https://aomediacodec.github.io/av1-spec/#obu-syntax
public record AV1OBU(ASByteView view) {

    public AV1OBU(ASByteView view) {
        int header = view.u8(0);
        boolean hasSizeField = (header & 0b10) != 0;

        if (!hasSizeField) {
            this.view = view;
        } else {
            // Slice the buffer.
            boolean hasExtension = (header & 0b100) != 0;
            int offset = (hasExtension ? 2 : 1);

            LEB128 payloadLength = LEB128.parse(view, offset);
            this.view = view.slice(0, Math.toIntExact(offset + payloadLength.value + payloadLength.byteWidth));
        }
    }

    public static AV1OBU from(byte[] data) {
        return new AV1OBU(new ASByteView(data));
    }

    public int fbz() {
        return (this.view.u8(0) >> 7) & 1;
    }

    public int rawType() {
        return this.view.u8(0) >> 3 & 0b1111;
    }

    public AV1OBUType type() {
        return AV1OBUType.LUT[this.rawType()];
    }

    public boolean hasExtension() {
        return (this.view.u8(0) >> 2 & 1) == 1;
    }

    public boolean hasSizeField() {
        return (this.view.u8(0) >> 1 & 1) == 1;
    }

    public boolean rsv() {
        return (this.view.u8(0) & 1) == 1;
    }

    public int temporalId() {
        if (!this.hasExtension()) return 0;
        return (this.view.u8(1) >> 5) & 0b111;
    }

    public int spatialId() {
        if (!this.hasExtension()) return 0;
        return (this.view.u8(1) >> 3) & 0b11;
    }

    public int extRsv3() {
        if (!this.hasExtension()) return 0;
        return this.view.u8(1) & 0b111;
    }

    public int headerLength() {
        int len = 1;

        if (this.hasExtension()) {
            len++;
        }
        if (this.hasSizeField()) {
            // https://aomediacodec.github.io/av1-spec/#leb128
            while (true) {
                int byteValue = this.view.u8(len);
                len++;
                if ((byteValue & 0b10000000) == 0) {
                    break;
                }
            }
        }
        return len;
    }

    public long dataLength() {
        if (!this.hasSizeField()) {
            // A sizeless OBU extends to the end of the buffer.
            return this.view.length() - this.headerLength();
        }

        int offset = this.hasExtension() ? 2 : 1;
        return LEB128.parse(this.view, offset).value;
    }

    public ASByteView dataView() {
        return this.view.slice(this.headerLength(), Math.toIntExact(this.dataLength()));
    }

    @Override
    public String toString() {
        return String.format(
            "AV1OBU[fbz=%d, type=%s (%d), temporalId=%d, spatialId=%d, headerLength=%dB, dataLength=%dB]",
            this.fbz(),
            this.type(), this.rawType(),
            this.temporalId(), this.spatialId(),
            this.headerLength(), this.dataLength()
        );
    }

    static record LEB128(long value, int byteWidth) {

        static LEB128 parse(ASByteView view, int offset) {
            // https://aomediacodec.github.io/av1-spec/#leb128
            long value = 0;
            int shift = 0;
            int byteWidth = 0;
            while (true) {
                int b = view.u8(offset++);
                value |= (long) (b & 0x7F) << shift;
                byteWidth++;

                if ((b & 0x80) == 0) {
                    break;
                }

                shift += 7;
                if (shift >= 56) {
                    throw new IllegalArgumentException("Invalid AV1 OBU size");
                }
            }

            return new LEB128(value, byteWidth);
        }

    }

}
