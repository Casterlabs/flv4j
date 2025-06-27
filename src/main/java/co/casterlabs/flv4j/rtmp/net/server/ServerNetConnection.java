package co.casterlabs.flv4j.rtmp.net.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import co.casterlabs.flv4j.actionscript.amf0.AMF0Type;
import co.casterlabs.flv4j.actionscript.amf0.Null0;
import co.casterlabs.flv4j.actionscript.amf0.Number0;
import co.casterlabs.flv4j.actionscript.amf0.Object0;
import co.casterlabs.flv4j.rtmp.RTMPReader;
import co.casterlabs.flv4j.rtmp.RTMPWriter;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessage;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessageChunkSize;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessageSetPeerBandwidth;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessageSetPeerBandwidth.LimitType;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessageUserControl;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessageWindowAcknowledgementSize;
import co.casterlabs.flv4j.rtmp.chunks.control.RTMPStreamBeginControlMessage;
import co.casterlabs.flv4j.rtmp.net.CallError;
import co.casterlabs.flv4j.rtmp.net.NetConnection;
import co.casterlabs.flv4j.rtmp.net.NetStatus;
import co.casterlabs.flv4j.rtmp.net.RTMPConnection;

public abstract class ServerNetConnection extends NetConnection {
    private static final int DEFAULT_CHUNK_SIZE = 4096;
    private static final int DEFAULT_WINDOW_ACK_SIZE = 2500000;

    final RTMPConnection conn;

    private AtomicInteger currStreamId = new AtomicInteger(1); // 0 is reserved for control.
    Map<Integer, ServerNetStream> streams = new HashMap<>();

    public ServerNetConnection(RTMPReader in, RTMPWriter out) {
        this.conn = new RTMPConnection(in, out);
        this.conn.onCall = this::onCall;
        this.conn.onMessage = this::onMessage;
    }

    public final void run() throws IOException, InterruptedException {
        try {
            this.conn.run();
        } finally {
            for (ServerNetStream stream : this.streams.values()) {
                try {
                    stream.deleteStream();
                } catch (IOException | InterruptedException | CallError ignored) {}
            }
        }
    }

    private final void onMessage(int msId, int timestamp, RTMPMessage message) {
        if (msId != RTMPConnection.CONTROL_MSID) {
            ServerNetStream stream = this.streams.get(msId);
            if (stream != null) {
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
            ServerNetStream stream = this.streams.get(msId);

            if (stream == null) {
                throw new CallError(NetStatus.NC_CALL_FAILED);
            }

            return stream.onCall(method, args);
        }

        switch (method) {
            case "connect": {
                Object0 res = this.connect(args);

                this.conn.setWindowAcknowledgementSize(DEFAULT_WINDOW_ACK_SIZE);

                this.sendMessage(
                    0,
                    new RTMPMessageWindowAcknowledgementSize(DEFAULT_WINDOW_ACK_SIZE)
                );
                this.sendMessage(
                    0,
                    new RTMPMessageSetPeerBandwidth(DEFAULT_WINDOW_ACK_SIZE, LimitType.DYNAMIC.id)
                );
                this.sendMessage(
                    0,
                    new RTMPMessageChunkSize(DEFAULT_CHUNK_SIZE)
                );
                this.sendMessage(
                    0,
                    new RTMPMessageUserControl(
                        0,
                        new RTMPStreamBeginControlMessage(0)
                    )
                );

                return new AMF0Type[] {
                        res,
                        NetStatus.NC_CONNECT_SUCCESS.asObject()
                };
            }

            case "createStream": {
                int streamId = this.currStreamId.getAndIncrement();

                ServerNetStream stream = this.createStream(args[0]);
                stream.id = streamId;
                stream.server = this;
                this.streams.put(streamId, stream);

                this.sendMessage(
                    0,
                    new RTMPMessageUserControl(
                        0,
                        new RTMPStreamBeginControlMessage(streamId)
                    )
                );

                return new AMF0Type[] {
                        Null0.INSTANCE,
                        new Number0(streamId)
                };
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

    public final int activeStreams() {
        return this.streams.size();
    }

    @Override
    public abstract ServerNetStream createStream(AMF0Type arg) throws IOException, InterruptedException, CallError;

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
