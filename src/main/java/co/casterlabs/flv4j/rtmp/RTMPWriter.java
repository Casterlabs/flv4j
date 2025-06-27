package co.casterlabs.flv4j.rtmp;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;

import co.casterlabs.flv4j.FLVSerializable;
import co.casterlabs.flv4j.actionscript.io.ASWriter;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessage;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessageChunkSize;
import co.casterlabs.flv4j.rtmp.handshake.RTMPHandshake0;
import co.casterlabs.flv4j.rtmp.handshake.RTMPHandshake1;
import co.casterlabs.flv4j.rtmp.handshake.RTMPHandshake2;
import lombok.RequiredArgsConstructor;

// https://rtmp.veriskope.com/pdf/rtmp_specification_1.0.pdf
@RequiredArgsConstructor
public class RTMPWriter {
    public static final long CONTROL_MSID = 0;
    public static final int CONTROL_CSID = 2;

    private final byte[] handshakeRandom = new byte[RTMPHandshake1.RANDOM_SIZE];
    private final ASWriter writer;

    private final ReentrantLock writeLock = new ReentrantLock();
    private final ChunkWriter[] chunkWriters = {
            new ChunkWriter(CONTROL_CSID + 0), // control
            new ChunkWriter(CONTROL_CSID + 1), // other
            new ChunkWriter(CONTROL_CSID + 2), // audio
            new ChunkWriter(CONTROL_CSID + 3), // video
    };

    private int chunkSize = 128;

    public void handshake0() throws IOException {
        new RTMPHandshake0(3).serialize(this.writer);
    }

    public void handshake1() throws IOException {
        ThreadLocalRandom.current().nextBytes(this.handshakeRandom);
        new RTMPHandshake1(0, this.handshakeRandom).serialize(this.writer);
    }

    public void handshake2(RTMPHandshake1 other) throws IOException {
        new RTMPHandshake2(
            other.epoch(),
            0,
            other.random()
        ).serialize(this.writer);
    }

    public boolean validateHandshake2(RTMPHandshake2 hs) {
        return Arrays.equals(hs.randomEcho(), this.handshakeRandom);
    }

    public void write(long msId, int timestamp, RTMPMessage message) throws IOException {
        ChunkWriter writer;
        if (message.isControl()) {
            writer = this.chunkWriters[0]; // we MUST send this over the control stream.
        } else if (message.rawType() == 8) {
            writer = this.chunkWriters[2]; // audio
        } else if (message.rawType() == 9) {
            writer = this.chunkWriters[3]; // video
        } else {
            writer = this.chunkWriters[1]; // other
        }

        writer.write(msId, timestamp, message);

        if (message instanceof RTMPMessageChunkSize chunkSize) {
            this.chunkSize = chunkSize.chunkSize();
        }
    }

    @RequiredArgsConstructor
    private class ChunkWriter {
        private final int chunkStreamId;
        private final ReentrantLock chunkLock = new ReentrantLock();

        private void write(long messageStreamId, int timestamp, RTMPMessage message) throws IOException {
            this.chunkLock.lock();
            try {
                int size = message.size();

                if (size <= chunkSize) {
                    // We can fit it in a single chunk!
                    write0(this.chunkStreamId, messageStreamId, timestamp, size, message.rawType(), message);
                    return;
                }

                // Chunking!
                byte[] b = message.raw();
                write0(this.chunkStreamId, messageStreamId, timestamp, size, message.rawType(), b, 0, chunkSize);

                int offset = chunkSize;
                while (b.length - offset > 0) {
                    int toWrite = Math.min(b.length - offset, chunkSize);
                    write3(this.chunkStreamId, b, offset, toWrite);
                    offset += toWrite;
                }
            } finally {
                this.chunkLock.unlock();
            }
        }

    }

    private void write0(int csId, long msId, int timestamp, int messageLength, int messageTypeId, byte[] bytes, int off, int len) throws IOException {
        this.writeLock.lock();
        try {
            int fb = 0 << 6 | (csId & 0b00111111);

            this.writer.u8(fb);
            // https://rtmp.veriskope.com/pdf/rtmp_specification_1.0.pdf#page=14
            int ts24 = timestamp;
            long ts32 = 0;
            if (timestamp >= 0xFFFFFF) {
                ts24 = 0xFFFFFF;
                ts32 = timestamp % 0xFFFFFFFF;
            }

            this.writer.u24(ts24);
            this.writer.u24(messageLength);
            this.writer.u8(messageTypeId);
            this.writer.u32le(msId);

            if (ts24 == 0xFFFFFF) {
                this.writer.u32(ts32);
            }

            this.writer.bytes(bytes, off, len);
        } finally {
            this.writeLock.unlock();
        }
    }

    private void write0(int csId, long msId, int timestamp, int messageLength, int messageTypeId, FLVSerializable ser) throws IOException {
        this.writeLock.lock();
        try {
            int fb = 0 << 6 | (csId & 0b00111111);

            this.writer.u8(fb);
            // https://rtmp.veriskope.com/pdf/rtmp_specification_1.0.pdf#page=14
            int now = timestamp % 0xFFFFFF;

            this.writer.u24(now);
            this.writer.u24(messageLength);
            this.writer.u8(messageTypeId);
            this.writer.u32le(msId);

            ser.serialize(this.writer);
        } finally {
            this.writeLock.unlock();
        }
    }

    private void write3(int csId, byte[] bytes, int off, int len) throws IOException {
        this.writeLock.lock();
        try {
            int fb = 3 << 6 | (csId & 0b00111111);
            this.writer.u8(fb);

            // https://rtmp.veriskope.com/pdf/rtmp_specification_1.0.pdf#page=15
            // (reuse all previous values)

            this.writer.bytes(bytes, off, len);
        } finally {
            this.writeLock.unlock();
        }
    }

}
