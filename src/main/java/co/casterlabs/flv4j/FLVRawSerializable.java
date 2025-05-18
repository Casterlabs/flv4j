package co.casterlabs.flv4j;

import java.io.IOException;

import co.casterlabs.flv4j.actionscript.io.ASWriter;
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
    default void serialize(ASWriter writer) throws IOException {
        writer.bytes(this.raw());
    }

}
