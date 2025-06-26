package co.casterlabs.flv4j.rtmp.net;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.flv4j.rtmp.chunks.RTMPMessage;

public abstract class NetStream extends RPCHandler {
    public @Nullable StatusHandler onStatus;

    public abstract int id();

    public void deleteStream() throws IOException, InterruptedException, CallError {}

    public abstract void publish(String name, String type) throws IOException, InterruptedException;

    /* ------------------------ */

    public abstract void sendMessage(int timestamp, RTMPMessage message) throws IOException, InterruptedException;

    @FunctionalInterface
    public interface StatusHandler {
        public void onStatus(NetStatus status) throws IOException, InterruptedException;
    }

}
