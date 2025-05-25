package co.casterlabs.flv4j.flv.tags.video.data.avc;

import co.casterlabs.flv4j.FLVRawSerializable;

// https://ossrs.net/lts/zh-cn/assets/files/ISO_IEC_14496-3-AAC-2001-7f4d0b3622b322cb72c78f85d91c449f.pdf#page=14
public record AVCVideoRawFrame(
    byte[] raw
) implements AVCVideoFrame, FLVRawSerializable {

    @Override
    public final String toString() {
        return String.format(
            "AVCVideoRawFrame[size=%d]",
            this.size()
        );
    }

}
