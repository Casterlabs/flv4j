package co.casterlabs.flv4j.amf0;

interface _Parser {
    static final _Parser RESERVED = (offset, bytes) -> {
        throw new UnsupportedOperationException("Reserved type.");
    };

    AMF0Type parse(int offset, byte[] bytes);

}
