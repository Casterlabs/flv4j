package co.casterlabs.flv4j.actionscript.amf0;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

// https://veovera.org/docs/legacy/amf0-file-format-spec.pdf#page=6
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ObjectEnd0 extends _MarkerType {
    public static final ObjectEnd0 INSTANCE = new ObjectEnd0();

    @Override
    public Type type() {
        return Type.OBJECT_END;
    }

}
