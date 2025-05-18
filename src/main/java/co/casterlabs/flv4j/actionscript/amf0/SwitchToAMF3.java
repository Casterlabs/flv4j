package co.casterlabs.flv4j.actionscript.amf0;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

// https://rtmp.veriskope.com/pdf/amf0-file-format-specification.pdf#page=8
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SwitchToAMF3 extends _MarkerType {
    public static final SwitchToAMF3 INSTANCE = new SwitchToAMF3();

    @Override
    public Type type() {
        return Type.SWITCH_TO_AMF3;
    }

}
