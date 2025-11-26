package co.casterlabs.flv4j.codecs.video.avc1;

import lombok.AllArgsConstructor;

//https://rtmp.veriskope.com/pdf/video_file_format_spec_v10.pdf#page=14
@AllArgsConstructor
public enum AVCPacketType {
    SEQUENCE_HEADER(0),
    NALU(1),
    END_OF_SEQUENCE(2),
    ;

    public static final AVCPacketType[] LUT = new AVCPacketType[255];
    static {
        for (AVCPacketType e : values()) {
            LUT[e.id] = e;
        }
    }

    public final int id;

}
