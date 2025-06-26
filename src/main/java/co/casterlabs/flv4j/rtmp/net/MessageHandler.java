package co.casterlabs.flv4j.rtmp.net;

import co.casterlabs.flv4j.rtmp.chunks.RTMPMessage;

@FunctionalInterface
public interface MessageHandler {
    public void onMessage(int msId, int timestamp, RTMPMessage message);
}