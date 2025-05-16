package co.casterlabs.flv4j.packets.payload.audio.data.aac;

import lombok.AllArgsConstructor;

//https://rtmp.veriskope.com/pdf/video_file_format_spec_v10.pdf#page=10
@AllArgsConstructor
public enum AACAudioDataType {
    SEQUENCE(0),
    RAW(1),
    ;

    public static final AACAudioDataType[] LUT = new AACAudioDataType[2];
    static {
        for (AACAudioDataType e : values()) {
            LUT[e.id] = e;
        }
    }

    public final int id;

}
