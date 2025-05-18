package co.casterlabs.flv4j.packets.payload.script;

import java.io.IOException;

import co.casterlabs.flv4j.actionscript.amf0.AMF0Type;
import co.casterlabs.flv4j.actionscript.amf0.ECMAArray0;
import co.casterlabs.flv4j.actionscript.amf0.String0;
import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASWriter;
import co.casterlabs.flv4j.packets.payload.FLVPayload;

// https://rtmp.veriskope.com/pdf/video_file_format_spec_v10.pdf#page=14 // WRONG!
// https://rtmp.veriskope.com/pdf/video_file_format_spec_v10_1.pdf#page=80 // THIS ONE IS CORRECT!
public record FLVScriptPayload(
    String0 method,
    ECMAArray0 value
) implements FLVPayload {

    public FLVScriptPayload(String method, ECMAArray0 value) {
        this(new String0(method), value);
    }

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
    public void serialize(ASWriter writer) throws IOException {
        this.method.serialize(writer);
        this.value.serialize(writer);
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
