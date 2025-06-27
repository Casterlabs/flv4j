package co.casterlabs.flv4j.actionscript.amf0;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import co.casterlabs.flv4j.FLVSerializable;
import co.casterlabs.flv4j.actionscript.io.ASReader;
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

    @SuppressWarnings("unchecked")
    public static <T extends AMF0Type> T parse(byte[] b, int off) throws IOException {
        int len = b.length - off;
        return (T) parse(new ASReader(b, off, len));
    }

    @SuppressWarnings("unchecked")
    public static <T extends AMF0Type> T parse(ASReader reader) throws IOException {
        Type type = Type.LUT[reader.u8()];
        return (T) type.parser.parse(reader);
    }

    // https://rtmp.veriskope.com/pdf/amf0-file-format-specification.pdf#page=4
    @AllArgsConstructor
    public enum Type {
        // @formatter:off
        NUMBER         ( 0, Number0::parse),
        BOOLEAN        ( 1, Boolean0::parse),
        STRING         ( 2, String0::parse),
        OBJECT         ( 3, Object0::parse),
        RESERVED_4     ( 4, _Parser.RESERVED),               // a.k.a "MOVIE_CLIP", was never used.
        NULL           ( 5, _MarkerType.parser(Null0.INSTANCE)),
        UNDEFINED      ( 6, _MarkerType.parser(Undefined0.INSTANCE)),
        REFERENCE      ( 7, Reference0::parse),
        ECMA_ARRAY     ( 8, ECMAArray0::parse),
        OBJECT_END     ( 9, _MarkerType.parser(ObjectEnd0.INSTANCE)),
        STRICT_ARRAY   (10, StrictArray0::parse),
        DATE           (11, Date0::parse),
        LONG_STRING    (12, LongString0::parse),
        UNSUPPORTED    (13, _MarkerType.parser(Unsupported0.INSTANCE)),
        RESERVED_14    (14, _Parser.RESERVED),               // a.k.a "RECORDSET", was never used.
        XML_DOCUMENT   (15, XMLDocument0::parse),
        TYPED_OBJECT   (16, TypedObject0::parse),
        SWITCH_TO_AMF3 (17, _MarkerType.parser(SwitchToAMF3.INSTANCE)),
        // @formatter:on
        ;

        public static final Type[] LUT = new Type[18];
        static {
            for (Type e : values()) {
                LUT[e.id] = e;
            }
        }

        public final int id;

        /**
         * @deprecated When the parser is used directly, the type marker should be
         *             consumed already.
         */
        @Deprecated
        public final _Parser parser;

    }

    public static interface ObjectLike extends AMF0Type {
        public Map<String, AMF0Type> map();
    }

    public static interface StringLike extends AMF0Type {
        public String value();
    }

}
