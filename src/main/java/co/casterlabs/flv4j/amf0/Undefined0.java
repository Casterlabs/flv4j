package co.casterlabs.flv4j.amf0;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

// https://rtmp.veriskope.com/pdf/amf0-file-format-specification.pdf#page=6
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Undefined0 extends _MarkerType {
    public static final Undefined0 INSTANCE = new Undefined0();

    @Override
    public Type type() {
        return Type.UNDEFINED;
    }

}
