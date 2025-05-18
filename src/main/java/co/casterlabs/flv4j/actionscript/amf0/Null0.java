package co.casterlabs.flv4j.actionscript.amf0;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

// https://rtmp.veriskope.com/pdf/amf0-file-format-specification.pdf#page=6
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Null0 extends _MarkerType {
    public static final Null0 INSTANCE = new Null0();

    @Override
    public Type type() {
        return Type.NULL;
    }

}
