package co.casterlabs.flv4j.rtmp.net.rpc;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.flv4j.actionscript.amf0.AMF0Type;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessage;
import co.casterlabs.flv4j.rtmp.chunks.control.RTMPControlMessage;

public abstract class RPCHandler {
    public @Nullable CallHandler onCall;
    public @Nullable MessageHandler onMessage;
    public @Nullable ControlHandler onControlMessage;

    public abstract void callVoid(String method, AMF0Type... args) throws IOException, InterruptedException;

    public abstract RPCPromise<AMF0Type[]> call(String method, AMF0Type... args) throws IOException, InterruptedException, CallError;

    @FunctionalInterface
    public interface CallHandler {
        public @Nullable AMF0Type[] onCall(String method, AMF0Type... args) throws IOException, InterruptedException, CallError;
    }

    @FunctionalInterface
    public interface MessageHandler {
        public void onMessage(int timestamp, RTMPMessage message);
    }

    @FunctionalInterface
    public interface ControlHandler {
        public void onControlMessage(RTMPControlMessage control);
    }

}
