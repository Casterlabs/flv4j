package co.casterlabs.flv4j.rtmp.net;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.flv4j.actionscript.amf0.AMF0Type;
import co.casterlabs.flv4j.actionscript.amf0.Number0;
import co.casterlabs.flv4j.actionscript.amf0.String0;

public abstract class RPCHandler {
    public static final int CONTROL_MSID = 0;
    public static final Number0 VOID_TSID = new Number0(0);

    public static final String0 _RESULT = new String0("_result");
    public static final String0 _ERROR = new String0("_error");

    public @Nullable CallHandler onCall;
    public @Nullable MessageHandler onMessage;

    public abstract void callVoid(String method, AMF0Type... args) throws IOException, InterruptedException;

    public abstract AMF0Type[] call(String method, AMF0Type... args) throws IOException, InterruptedException, CallError;

    @FunctionalInterface
    public interface CallHandler {
        public @Nullable AMF0Type[] onCall(int msId, String method, AMF0Type... args) throws IOException, InterruptedException, CallError;
    }

}
