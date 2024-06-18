package co.casterlabs.flv4j.util;

import java.io.IOException;
import java.io.InputStream;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ThrowOnMinus1InputStream extends InputStream {
    private InputStream wrapped;

    /**
     * See: {@link InputStream#read()}
     */
    @Override
    public int read() throws IOException {
        int read = this.wrapped.read();
        if (read == -1) throw new EndOfStreamException("End of stream");
        return read;
    }

    /**
     * See: {@link InputStream#read(byte[], int, int)}
     */
    @Override
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        int read = this.wrapped.read(b, off, len);
        if (read == -1) throw new EndOfStreamException("End of stream");
        return read;
    }

    /**
     * See: {@link InputStream#available()}
     */
    @Override
    public synchronized int available() throws IOException {
        return this.wrapped.available();
    }

    /**
     * See: {@link InputStream#close()}
     */
    @Override
    public void close() throws IOException {
        this.wrapped.close();
    }

}
