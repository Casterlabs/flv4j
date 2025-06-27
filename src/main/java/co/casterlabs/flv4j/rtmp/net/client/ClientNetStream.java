package co.casterlabs.flv4j.rtmp.net.client;

import java.io.IOException;

import co.casterlabs.flv4j.actionscript.amf0.AMF0Type;
import co.casterlabs.flv4j.actionscript.amf0.AMF0Type.ObjectLike;
import co.casterlabs.flv4j.actionscript.amf0.Boolean0;
import co.casterlabs.flv4j.actionscript.amf0.Null0;
import co.casterlabs.flv4j.actionscript.amf0.Number0;
import co.casterlabs.flv4j.actionscript.amf0.String0;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessage;
import co.casterlabs.flv4j.rtmp.net.NetStatus;
import co.casterlabs.flv4j.rtmp.net.NetStream;
import co.casterlabs.flv4j.rtmp.net.rpc.CallError;
import co.casterlabs.flv4j.rtmp.net.rpc.RPCPromise;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class ClientNetStream extends NetStream {
    private final ClientNetConnection client;
    private final int id;

    @Override
    public final int id() {
        return this.id;
    }

    /* ------------------------ */

    final AMF0Type[] onCall(String method, AMF0Type... args) throws IOException, InterruptedException, CallError {
        switch (method) {
            case "onStatus": {
                NetStatus status = new NetStatus((ObjectLike) args[1]);
                this.setStatus(status);
                return null;
            }

            default:
                if (this.onCall == null) {
                    throw new CallError(NetStatus.NC_CALL_FAILED);
                } else {
                    return this.onCall.onCall(method, args);
                }
        }
    }

    /* ------------------------ */

    @Override
    public void play(String name, double start, double duration, boolean reset) throws IOException, InterruptedException {
        this.callVoid(
            "play",
            Null0.INSTANCE,
            new String0(name),
            new Number0(start),
            new Number0(duration),
            Boolean0.valueOf(reset)
        );
    }

    // TODO play2

    @Override
    public void receiveAudio(boolean enabled) throws IOException, InterruptedException {
        this.callVoid(
            "receiveAudio",
            Null0.INSTANCE,
            Boolean0.valueOf(enabled)
        );
    }

    @Override
    public void receiveVideo(boolean enabled) throws IOException, InterruptedException {
        this.callVoid(
            "receiveVideo",
            Null0.INSTANCE,
            Boolean0.valueOf(enabled)
        );
    }

    @Override
    public void publish(String name, String type) throws IOException, InterruptedException {
        this.callVoid(
            "publish",
            Null0.INSTANCE,
            new String0(name),
            new String0(type)
        );
    }

    @Override
    public void seek(long milliseconds) throws IOException, InterruptedException {
        this.callVoid(
            "seek",
            Null0.INSTANCE,
            new Number0(milliseconds)
        );
    }

    @Override
    public void pause(boolean pause, long milliseconds) throws IOException, InterruptedException {
        this.callVoid(
            "pause",
            Null0.INSTANCE,
            Boolean0.valueOf(pause),
            new Number0(milliseconds)
        );
    }

    @Override
    public void deleteStream() throws IOException, InterruptedException {
        this.callVoid(
            "deleteStream",
            Null0.INSTANCE,
            new Number0(this.id)
        );
    }

    /* ------------------------ */

    @Override
    public final void sendMessage(int timestamp, RTMPMessage message) throws IOException, InterruptedException {
        this.client.conn.sendMessage(
            this.id,
            timestamp,
            message
        );
    }

    @Override
    public final void callVoid(String method, AMF0Type... args) throws IOException, InterruptedException {
        this.client.conn.callVoid(
            this.id,
            method,
            args
        );
    }

    @Override
    public final RPCPromise<AMF0Type[]> call(String method, AMF0Type... args) throws IOException, InterruptedException, CallError {
        return this.client.conn.call(
            this.id,
            method,
            args
        );
    }

}
