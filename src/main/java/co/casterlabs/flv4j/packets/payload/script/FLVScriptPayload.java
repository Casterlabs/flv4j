package co.casterlabs.flv4j.packets.payload.script;

import java.io.IOException;
import java.io.OutputStream;

import co.casterlabs.flv4j.amf0.AMF0Type;
import co.casterlabs.flv4j.amf0.ECMAArray0;
import co.casterlabs.flv4j.amf0.String0;
import co.casterlabs.flv4j.packets.payload.FLVPayload;
import co.casterlabs.flv4j.util.ASReader;

// https://rtmp.veriskope.com/pdf/video_file_format_spec_v10.pdf#page=14 // WRONG!
// https://rtmp.veriskope.com/pdf/video_file_format_spec_v10_1.pdf#page=80 // THIS ONE IS CORRECT!
public record FLVScriptPayload(
    String0 method,
    ECMAArray0 value
) implements FLVPayload {

    public String methodName() {
        return this.method().value();
    }

    @Override
    public boolean isSequenceHeader() {
        return true; // ?
    }

    @Override
    public int size() {
        return this.method.size() + this.value.size();
    }

    @Override
    public void serialize(OutputStream out) throws IOException {
        this.method.serialize(out);
        this.value.serialize(out);
    }

    public static FLVScriptPayload parse(ASReader reader) throws IOException {
        String0 method = AMF0Type.parse(reader);
        ECMAArray0 value = AMF0Type.parse(reader);
        return new FLVScriptPayload(method, value);
    }

    @Override
    public final String toString() {
        return String.format(
            "FLVScriptPayload[%s(%s)]",
            this.methodName(), this.value
        );
    }

}
