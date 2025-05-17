package co.casterlabs.flv4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public interface FLVSerializable {

    public int size();

    default byte[] raw() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(this.size());
        this.serialize(baos);
        return baos.toByteArray();
    }

    public void serialize(OutputStream out) throws IOException;

}
