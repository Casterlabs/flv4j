package co.casterlabs.flv4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import co.casterlabs.flv4j.actionscript.io.ASWriter;
import co.casterlabs.flv4j.documentation.CreatesCopy;

public interface FLVSerializable {

    public int size();

    @CreatesCopy
    default byte[] raw() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(this.size());
        this.serialize(new ASWriter(baos));
        return baos.toByteArray();
    }

    public void serialize(ASWriter writer) throws IOException;

}
