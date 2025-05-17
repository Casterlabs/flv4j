package co.casterlabs.flv4j;

import java.io.IOException;
import java.io.OutputStream;

import lombok.SneakyThrows;

public interface FLVRawSerializable extends FLVSerializable {

    @Override
    public byte[] raw() throws IOException;

    @SneakyThrows // Should never be thrown.
    @Override
    default int size() {
        return this.raw().length;
    }

    @Override
    default void serialize(OutputStream out) throws IOException {
        out.write(this.raw());
    }

}
