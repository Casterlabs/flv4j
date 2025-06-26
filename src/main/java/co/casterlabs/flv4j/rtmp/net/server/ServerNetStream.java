package co.casterlabs.flv4j.rtmp.net.server;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import co.casterlabs.flv4j.actionscript.amf0.AMF0Type;
import co.casterlabs.flv4j.actionscript.amf0.Null0;
import co.casterlabs.flv4j.actionscript.amf0.Number0;
import co.casterlabs.flv4j.actionscript.amf0.String0;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessage;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessageCommand0;
import co.casterlabs.flv4j.rtmp.net.CallError;
import co.casterlabs.flv4j.rtmp.net.NetStatus;
import co.casterlabs.flv4j.rtmp.net.NetStream;
import lombok.SneakyThrows;

public abstract class ServerNetStream extends NetStream {
    NetConnectionServer server;
    int id;

    {
        this.onStatus = (status) -> {
            this.callVoid("onStatus", Null0.INSTANCE, status.asObject());
        };
    }

    @Override
    public final int id() {
        return this.id;
    }

    /* ------------------------ */

    final AMF0Type[] onCall(String method, AMF0Type... args) throws IOException, InterruptedException, CallError {
        switch (method) {
            case "deleteStream": {
                this.server.streams.remove(this.id);
                this.deleteStream();
                return null;
            }

            case "publish": {
                String name = ((String0) args[1]).value();
                String type = ((String0) args[2]).value();
                this.publish(name, type);
                return null;
            }

            default:
                if (this.onCall == null) {
                    throw new CallError(NetStatus.NC_CALL_FAILED);
                } else {
                    return this.onCall.onCall(this.id, method, args);
                }
        }
    }

    /* ------------------------ */

    @Override
    public final void sendMessage(int timestamp, RTMPMessage message) throws IOException, InterruptedException {
        if (this.id == 0 || this.server == null) {
            throw new IllegalStateException("Cannot send NetStream message before it has been initialized.");
        }

        this.server.sendMessage(
            this.id,
            timestamp,
            message
        );
    }

    @Override
    public final void callVoid(String method, AMF0Type... args) throws IOException, InterruptedException {
        this.sendMessage(
            0, // ?
            new RTMPMessageCommand0(
                new String0(method),
                VOID_TSID,
                Arrays.asList(args)
            )
        );
    }

    @SneakyThrows
    @Override
    public final AMF0Type[] call(String method, AMF0Type... args) throws IOException, InterruptedException, CallError {
        CompletableFuture<AMF0Type[]> future = new CompletableFuture<>();

        int tsId = this.server.currTsId.getAndIncrement();
        this.server.rpcFutures.put(tsId, future);

        this.sendMessage(
            0, // ?
            new RTMPMessageCommand0(
                new String0(method),
                new Number0(tsId),
                Arrays.asList(args)
            )
        );

        try {
            return future.get();
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

}
