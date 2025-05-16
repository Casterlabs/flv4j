package co.casterlabs.flv4j.packets.payload.video.data.avc;

import lombok.AllArgsConstructor;

//https://rtmp.veriskope.com/pdf/video_file_format_spec_v10.pdf#page=14
@AllArgsConstructor
public enum AVCVideoDataType {
    SEQUENCE(0),
    NALU(1),
    SEQUENCE_END(2),
    ;

    public static final AVCVideoDataType[] LUT = new AVCVideoDataType[3];
    static {
        for (AVCVideoDataType e : values()) {
            LUT[e.id] = e;
        }
    }

    public final int id;

}
