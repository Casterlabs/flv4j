package co.casterlabs.flv4j.actionscript.amf0;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

// https://rtmp.veriskope.com/pdf/amf0-file-format-specification.pdf#page=7
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Unsupported0 extends _MarkerType {
    public static final Unsupported0 INSTANCE = new Unsupported0();

    @Override
    public Type type() {
        return Type.UNSUPPORTED;
    }

}
