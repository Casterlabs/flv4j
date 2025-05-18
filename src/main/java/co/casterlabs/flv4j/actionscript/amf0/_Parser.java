package co.casterlabs.flv4j.actionscript.amf0;

import java.io.IOException;

import co.casterlabs.flv4j.actionscript.io.ASReader;

interface _Parser {
    static final _Parser RESERVED = (reader) -> {
        throw new UnsupportedOperationException("Reserved type.");
    };

    AMF0Type parse(ASReader reader) throws IOException;

}
