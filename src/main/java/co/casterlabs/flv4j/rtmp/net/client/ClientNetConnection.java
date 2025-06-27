package co.casterlabs.flv4j.rtmp.net.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.flv4j.actionscript.amf0.AMF0Type;
import co.casterlabs.flv4j.actionscript.amf0.AMF0Type.ObjectLike;
import co.casterlabs.flv4j.actionscript.amf0.Number0;
import co.casterlabs.flv4j.rtmp.RTMPReader;
import co.casterlabs.flv4j.rtmp.RTMPWriter;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessage;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessageChunkSize;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessageUserControl;
import co.casterlabs.flv4j.rtmp.chunks.control.RTMPControlMessage;
import co.casterlabs.flv4j.rtmp.chunks.control.RTMPControlMessageStream;
import co.casterlabs.flv4j.rtmp.chunks.control.RTMPPingRequestControlMessage;
import co.casterlabs.flv4j.rtmp.chunks.control.RTMPPingResponseControlMessage;
import co.casterlabs.flv4j.rtmp.net.CallError;
import co.casterlabs.flv4j.rtmp.net.ConnectArgs;
import co.casterlabs.flv4j.rtmp.net.NetConnection;
import co.casterlabs.flv4j.rtmp.net.NetStatus;
import co.casterlabs.flv4j.rtmp.net.NetStream;
import co.casterlabs.flv4j.rtmp.net.RTMPConnection;

public abstract class ClientNetConnection extends NetConnection {
    final RTMPConnection conn;

    private Map<Integer, ClientNetStream> streams = new HashMap<>();

    public ClientNetConnection(RTMPReader in, RTMPWriter out) {
        this.conn = new RTMPConnection(in, out);
        this.conn.onCall = this::onCall;
        this.conn.onMessage = this::onMessage;
        this.conn.onControlMessage = this::onControlMessage;
    }

    /**
     * @param    args should contain AT LEAST an Object0/ECMAArray0.
     * 
     * @implNote      this class only supports amf0 object encoding.
     * 
     * @see           https://rtmp.veriskope.com/docs/spec/#7211connect
     */
    @Override
    public ObjectLike connect(ConnectArgs args) throws IOException, InterruptedException, CallError {
        return this.connect(args, Thread::new);
    }

    /**
     * @implNote this class only supports amf0 object encoding.
     * 
     * @see      https://rtmp.veriskope.com/docs/spec/#7211connect
     */
    public final ObjectLike connect(ConnectArgs args, ThreadFactory factory) throws IOException, InterruptedException, CallError {
        if (args.objectEncoding() != 0) {
            throw new IllegalArgumentException("Only amf0 object encoding is supported.");
        }

        this.conn.handshake();

        factory.newThread(() -> {
            try {
                this.conn.run();
                this.onClose(null);
            } catch (Throwable t) {
                this.onClose(t);
            }
        }).start();

        return (ObjectLike) this.call("connect", args.toAMF0())[0];
    }

    private final void onControlMessage(int msId, RTMPControlMessage control) {
        if (control instanceof RTMPPingRequestControlMessage ping) {
            try {
                this.sendMessage(
                    0,
                    new RTMPMessageUserControl(
                        new RTMPPingResponseControlMessage(ping.timestamp())
                    )
                );
            } catch (IOException | InterruptedException ignored) {}
        } else if (control instanceof RTMPControlMessageStream streamControl) {
            ClientNetStream stream = this.streams.get((int) streamControl.streamId());
            if (stream != null && stream.onControlMessage != null) {
                stream.onControlMessage.onControlMessage(streamControl);
            }
        } else {
            if (this.onControlMessage != null) {
                this.onControlMessage.onControlMessage(control);
            }
        }
    }

    private final void onMessage(int msId, int timestamp, RTMPMessage message) {
        if (message instanceof RTMPMessageChunkSize) {
            try {
                this.sendMessage(timestamp, message); // Echo it back :^)
            } catch (IOException | InterruptedException e) {}
            return;
        }

        if (msId != RTMPConnection.CONTROL_MSID) {
            ClientNetStream stream = this.streams.get(msId);
            if (stream != null && stream.onMessage != null) {
                stream.onMessage.onMessage(timestamp, message);
            }
            return;
        }

        if (this.onMessage != null) {
            this.onMessage.onMessage(timestamp, message);
        }
    }

    private final AMF0Type[] onCall(int msId, String method, AMF0Type... args) throws IOException, InterruptedException, CallError {
        if (msId != RTMPConnection.CONTROL_MSID) {
            ClientNetStream stream = this.streams.get(msId);

            if (stream == null) {
                throw new CallError(NetStatus.NC_CALL_FAILED);
            }

            return stream.onCall(method, args);
        }

        switch (method) {
            default:
                if (this.onCall == null) {
                    throw new CallError(NetStatus.NC_CALL_FAILED);
                } else {
                    return this.onCall.onCall(method, args);
                }
        }
    }

    /* ------------------------ */

    public final int activeStreams() {
        return this.streams.size();
    }

    @Override
    public final NetStream createStream(AMF0Type arg) throws IOException, InterruptedException, CallError {
        AMF0Type[] result = this.call("createStream", arg); // per-spec, arg0 is a null type.
        int streamId = (int) ((Number0) result[1]).value();
        return new ClientNetStream(this, streamId);
    }

    public abstract void onClose(@Nullable Throwable reason);

    /* ------------------------ */

    @Override
    public final void sendMessage(int timestamp, RTMPMessage message) throws IOException, InterruptedException {
        this.conn.sendMessage(
            RTMPConnection.CONTROL_MSID,
            timestamp,
            message
        );
    }

    @Override
    public final void callVoid(String method, AMF0Type... args) throws IOException, InterruptedException {
        this.conn.callVoid(
            RTMPConnection.CONTROL_MSID,
            method,
            args
        );
    }

    @Override
    public final AMF0Type[] call(String method, AMF0Type... args) throws IOException, InterruptedException, CallError {
        return this.conn.call(RTMPConnection.CONTROL_MSID, method, args);
    }

}
