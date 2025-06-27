package co.casterlabs.flv4j.rtmp.net;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.flv4j.rtmp.chunks.RTMPMessage;
import co.casterlabs.flv4j.rtmp.net.rpc.CallError;
import co.casterlabs.flv4j.rtmp.net.rpc.RPCHandler;
import lombok.SneakyThrows;

// https://rtmp.veriskope.com/docs/spec/#722netstream-commands
public abstract class NetStream extends RPCHandler {
    public @Nullable StatusHandler onStatus;

    private NetStatus status;

    public final NetStatus status() {
        return this.status;
    }

    protected void setStatus(NetStatus status) {
        this.status = status;

        if (this.onStatus != null) {
            try {
                this.onStatus.onStatus(status);
            } catch (IOException | InterruptedException ignored) {}
        }
    }

    public abstract int id();

    /* ------------------------ */

    public final void play(String name) throws IOException, InterruptedException {
        this.play(name, -2);
    }

    public final void play(String name, double start) throws IOException, InterruptedException {
        this.play(name, start, -1, true);
    }

    public final void play(String name, double start, double duration) throws IOException, InterruptedException {
        this.play(name, start, duration, true);
    }

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

    public void deleteStream() throws IOException, InterruptedException {}

    /* ------------------------ */

    public abstract void sendMessage(int timestamp, RTMPMessage message) throws IOException, InterruptedException;

    @FunctionalInterface
    public interface StatusHandler {
        public void onStatus(NetStatus status) throws IOException, InterruptedException;
    }

}
