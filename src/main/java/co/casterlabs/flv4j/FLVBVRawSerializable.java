package co.casterlabs.flv4j;

import java.io.IOException;

import co.casterlabs.flv4j.actionscript.io.ASByteView;
import co.casterlabs.flv4j.actionscript.io.ASWriter;
import co.casterlabs.flv4j.documentation.CreatesCopy;

public interface FLVBVRawSerializable extends FLVSerializable {

    public ASByteView view();

    @Override
    @CreatesCopy
    default byte[] raw() {
        return this.view().raw();
    }

    @Override
    default int size() {
        return this.view().length();
    }

    @Override
    default void serialize(ASWriter writer) throws IOException {
        writer.bytes(this.view());
    }

}
