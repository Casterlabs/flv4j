package co.casterlabs.flv4j.rtmp;

class ChunkInProgress {
    final byte[] buffer;
    private int writeOffset = 0;

    ChunkInProgress(int length) {
        this.buffer = new byte[length];
    }

    int remaining() {
        return this.buffer.length - this.writeOffset;
    }

    /**
     * @return true if more data is needed.
     */
    boolean append(byte[] bytes) {
        System.arraycopy(bytes, 0, this.buffer, this.writeOffset, bytes.length);
        this.writeOffset += bytes.length;

        return this.writeOffset < this.buffer.length;
    }

}
