package co.casterlabs.flv4j.packets.payload.video.data.avc;

// https://ossrs.net/lts/zh-cn/assets/files/ISO_IEC_14496-3-AAC-2001-7f4d0b3622b322cb72c78f85d91c449f.pdf#page=14
public record AVCVideoRawFrame(
    byte[] raw
) implements AVCVideoFrame {

    @Override
    public int size() {
        return this.raw.length;
    }

    @Override
    public final String toString() {
        return String.format(
            "AVCVideoRawFrame[size=%d]",
            this.size()
        );
    }

}
