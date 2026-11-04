package co.casterlabs.flv4j.codecs.video;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ChromaSubsampling {
    CS_400("4:0:0"), // A.K.A. monochrome
    CS_420("4:2:0"),
    CS_422("4:2:2"),
    CS_440("4:4:0"),
    CS_444("4:4:4"),

    UNSPECIFIED("unspecified"),
    RESERVED("reserved"),
    UNKNOWN("unknown");

    public final String id;

}
