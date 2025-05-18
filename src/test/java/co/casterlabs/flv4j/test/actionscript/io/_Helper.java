package co.casterlabs.flv4j.test.actionscript.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import co.casterlabs.flv4j.actionscript.io.ASAssert;
import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASWriter;
import lombok.SneakyThrows;

class _Helper {
    private static final ByteArrayOutputStream reWriterDest = new ByteArrayOutputStream();
    private static final LateInputStream rwReaderSrc = new LateInputStream();

    private static final ASWriter rwWriter = new ASWriter(reWriterDest);
    private static final ASReader rwReader = new ASReader(rwReaderSrc);

    static {
        ASAssert.class.getClassLoader().setPackageAssertionStatus("co.casterlabs.flv4j", true);
    }

    @SneakyThrows
    public static synchronized byte[] rw(Action action) {
        reWriterDest.reset();
        rwReaderSrc.reset();

        action.run(rwWriter, rwReader);

        return reWriterDest.toByteArray();
    }

    public static interface Action {
        public void run(ASWriter writer, ASReader reader) throws Throwable;
    }

    /**
     * This class is used to delay the initialization of the reader's input stream.
     * Allowing it to be reset.
     */
    private static class LateInputStream extends InputStream {
        private ByteArrayInputStream wrapped;

        private void lateInit() {
            if (this.wrapped != null) return;
            this.wrapped = new ByteArrayInputStream(reWriterDest.toByteArray());
        }

        @Override
        public int read() throws IOException {
            this.lateInit();
            return this.wrapped.read();
        }

        @Override
        public synchronized int read(byte[] b, int off, int len) throws IOException {
            this.lateInit();
            return this.wrapped.read(b, off, len);
        }

        @Override
        public synchronized int available() throws IOException {
            this.lateInit();
            return this.wrapped.available();
        }

        @Override
        public void close() throws IOException {
            this.reset();
        }

        @Override
        public void reset() {
            this.wrapped = null;
        }

    }

}
