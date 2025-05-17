package co.casterlabs.flv4j.amf0;

import java.nio.charset.StandardCharsets;

import co.casterlabs.flv4j.FLVSerializable;
import lombok.AllArgsConstructor;
import lombok.NonNull;

public interface AMF0Type extends FLVSerializable {

    default boolean isComplex() {
        return false;
    }

    public Type type();

    /**
     * A helper method for allocating strings. It looks at the length of the string
     * and returns either a {@link LongString0} or a {@link String0}.
     * 
     * @return Either a {@link LongString0} or a {@link String0}.
     */
    public static AMF0Type allocateString(@NonNull String value) {
        int byteLength = value.getBytes(StandardCharsets.UTF_8).length;
        if (byteLength > 65535) {
            return new LongString0(value);
        } else {
            return new String0(value);
        }
    }

    // https://rtmp.veriskope.com/pdf/amf0-file-format-specification.pdf#page=4
    @AllArgsConstructor
    public enum Type {
        // @formatter:off
        NUMBER         ( 0, Number0::from),
        BOOLEAN        ( 1, Boolean0::from),
        STRING         ( 2, String0::from),
        OBJECT         ( 3, Object0::from),
        RESERVED_4     ( 4, _Parser.RESERVED),               // a.k.a "MOVIE_CLIP", was never used.
        NULL           ( 5, (o,b) -> Null0.INSTANCE),
        UNDEFINED      ( 6, (o,b) -> Undefined0.INSTANCE),
        REFERENCE      ( 7, Reference0::from),
        ECMA_ARRAY     ( 8, ECMAArray0::from),
        OBJECT_END     ( 9, (o,b) -> ObjectEnd0.INSTANCE),
        STRICT_ARRAY   (10, StrictArray0::from),
        DATE           (11, Date0::from),
        LONG_STRING    (12, LongString0::from),
        UNSUPPORTED    (13, (o,b) -> Unsupported0.INSTANCE),
        RESERVED_14    (14, _Parser.RESERVED),               // a.k.a "RECORDSET", was never used.
        XML_DOCUMENT   (15, XMLDocument0::from),
        TYPED_OBJECT   (16, TypedObject0::from),
        SWITCH_TO_AMF3 (17, (o,b) -> SwitchToAMF3.INSTANCE),
        // @formatter:on
        ;

        public static final Type[] LUT = new Type[18];
        static {
            for (Type e : values()) {
                LUT[e.id] = e;
            }
        }

        public final int id;
        private final _Parser parser;

        @SuppressWarnings("unchecked")
        public <T extends AMF0Type> T parse(int offset, byte[] bytes) {
            return (T) this.parser.parse(offset, bytes);
        }

    }

}
