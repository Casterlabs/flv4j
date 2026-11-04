package co.casterlabs.flv4j.actionscript.io;

public final class ASBitReader {
    private final ASByteView view;
    private int pos = 0;

    public ASBitReader(ASByteView view) {
        this.view = view;
    }

    public int bitsRead() {
        return this.pos;
    }

    public int bitsRemaining() {
        return this.view.length() * 8 - this.pos;
    }

    public boolean hasBits(int n) {
        return this.bitsRemaining() >= n;
    }

    public int bit() {
        if (!this.hasBits(1)) {
            throw new IllegalArgumentException("End of bit stream");
        }

        int value = (this.view.u8(this.pos >> 3) >> (7 - (this.pos & 0b111))) & 0b1;
        this.pos++;
        return value;
    }

    public long u(int n) {
        long value = 0;
        for (int i = 0; i < n; i++) {
            value = (value << 1) | this.bit();
        }
        return value;
    }

    public void skip(int n) {
        if (!this.hasBits(n)) {
            throw new IllegalArgumentException("End of bit stream");
        }
        this.pos += n;
    }

    public void skipTo(int pos) {
        if (pos < 0 || pos > this.view.length() * 8) {
            throw new IllegalArgumentException("End of bit stream");
        }
        this.pos = pos;
    }

    public int ue() {
        int zeros = 0;
        while (this.bit() == 0) {
            zeros++;
            if (zeros > 31) {
                throw new IllegalArgumentException("ue(v) run of zeros exceeds 31");
            }
        }
        return (1 << zeros) - 1 + (int) this.u(zeros);
    }

    public int se() {
        int k = this.ue();
        return (k & 0b1) != 0 ? (k + 1) / 2 : -(k / 2);
    }

}
