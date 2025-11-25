package co.casterlabs.flv4j.flv.tags.video;

public class RawVideoData implements VideoData {
    static final RawVideoData INSTANCE = new RawVideoData();

    @Override
    public boolean isSequenceHeader() {
        return false;
    }

    @Override
    public int compositionTimeOffset() {
        return 0;
    }

}
