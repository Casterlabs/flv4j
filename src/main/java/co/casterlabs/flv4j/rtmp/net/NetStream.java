package co.casterlabs.flv4j.rtmp.net;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.flv4j.rtmp.chunks.RTMPMessage;
import lombok.SneakyThrows;

public abstract class NetStream extends RPCHandler {
    public @Nullable StatusHandler onStatus;

    public abstract int id();

    public void deleteStream() throws IOException, InterruptedException, CallError {}

    /* ------------------------ */

    @SneakyThrows
    public void play(String name, double start, double duration, boolean reset) throws IOException, InterruptedException {
        throw new CallError(NetStatus.NC_CALL_FAILED);
    }

    // TODO play2

    @SneakyThrows
    public void receiveAudio(boolean enabled) throws IOException, InterruptedException {
        throw new CallError(NetStatus.NC_CALL_FAILED);
    }

    @SneakyThrows
    public void receiveVideo(boolean enabled) throws IOException, InterruptedException {
        throw new CallError(NetStatus.NC_CALL_FAILED);
    }

    @SneakyThrows
    public void publish(String name, String type) throws IOException, InterruptedException {
        throw new CallError(NetStatus.NC_CALL_FAILED);
    }

    @SneakyThrows
    public void seek(long milliseconds) throws IOException, InterruptedException {
        throw new CallError(NetStatus.NC_CALL_FAILED);
    }

    @SneakyThrows
    public void pause(boolean pause, long milliseconds) throws IOException, InterruptedException {
        throw new CallError(NetStatus.NC_CALL_FAILED);
    }

    /* ------------------------ */

    public abstract void sendMessage(int timestamp, RTMPMessage message) throws IOException, InterruptedException;

    @FunctionalInterface
    public interface StatusHandler {
        public void onStatus(NetStatus status) throws IOException, InterruptedException;
    }

}
