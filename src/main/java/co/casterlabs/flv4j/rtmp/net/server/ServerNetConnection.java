package co.casterlabs.flv4j.rtmp.net.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import co.casterlabs.flv4j.actionscript.amf0.AMF0Type;
import co.casterlabs.flv4j.actionscript.amf0.AMF0Type.ObjectLike;
import co.casterlabs.flv4j.actionscript.amf0.Null0;
import co.casterlabs.flv4j.actionscript.amf0.Number0;
import co.casterlabs.flv4j.rtmp.RTMPReader;
import co.casterlabs.flv4j.rtmp.RTMPWriter;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessage;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessageChunkSize;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessageSetPeerBandwidth;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessageSetPeerBandwidth.LimitType;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessageUserControl;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessageWindowAcknowledgementSize;
import co.casterlabs.flv4j.rtmp.chunks.control.RTMPControlMessage;
import co.casterlabs.flv4j.rtmp.chunks.control.RTMPControlMessageStream;
import co.casterlabs.flv4j.rtmp.chunks.control.RTMPPingRequestControlMessage;
import co.casterlabs.flv4j.rtmp.chunks.control.RTMPPingResponseControlMessage;
import co.casterlabs.flv4j.rtmp.chunks.control.RTMPStreamBeginControlMessage;
import co.casterlabs.flv4j.rtmp.net.CallError;
import co.casterlabs.flv4j.rtmp.net.ConnectArgs;
import co.casterlabs.flv4j.rtmp.net.NetConnection;
import co.casterlabs.flv4j.rtmp.net.NetStatus;
import co.casterlabs.flv4j.rtmp.net.RTMPConnection;

public abstract class ServerNetConnection extends NetConnection {
    private static final long PING_INTERVAL = TimeUnit.SECONDS.toMillis(30);

    private static final int DEFAULT_CHUNK_SIZE = 4096;
    private static final int DEFAULT_WINDOW_ACK_SIZE = 10_000_000; // 10mb

    final RTMPConnection conn;

    private AtomicInteger currStreamId = new AtomicInteger(1); // 0 is reserved for control.
    Map<Integer, ServerNetStream> streams = new HashMap<>();

    private long latency = -1;

    public ServerNetConnection(RTMPReader in, RTMPWriter out) {
        this.conn = new RTMPConnection(in, out);
        this.conn.onCall = this::onCall;
        this.conn.onMessage = this::onMessage;
        this.conn.onControlMessage = this::onControlMessage;
    }

    public final void handle() throws IOException, InterruptedException {
        this.handle(Thread::new);
    }

    public final void handle(ThreadFactory factory) throws IOException, InterruptedException {
        boolean[] $closed = new boolean[1];
        try {
            this.conn.handshake();

            factory.newThread(() -> {
                try {
                    while (!$closed[0]) {
                        // 24 bit timestamp, it's arbitrary since we're the one decoding it.
                        long timestamp = System.currentTimeMillis() & 0xFFFFFF;

                        this.sendMessage(
                            0,
                            new RTMPMessageUserControl(
                                new RTMPPingRequestControlMessage(timestamp)
                            )
                        );

                        Thread.sleep(PING_INTERVAL);
                    }
                } catch (InterruptedException | IOException ignored) {}
            }).start();

            this.conn.run();
        } finally {
            $closed[0] = true;
            for (ServerNetStream stream : this.streams.values()) {
                try {
                    stream.deleteStream();
                } catch (IOException | InterruptedException ignored) {}
            }
        }
    }

    public long latency() {
        return this.latency;
    }

    private final void onControlMessage(int msId, RTMPControlMessage control) {
        if (control instanceof RTMPPingResponseControlMessage ping) {
            long timestamp = ping.timestamp();
            long now = System.currentTimeMillis() & 0xFFFFFF; // 24 bit timestamp, it's arbitrary since we're the one decoding it.
            this.latency = now - timestamp;
        } else if (control instanceof RTMPControlMessageStream streamControl) {
            ServerNetStream stream = this.streams.get((int) streamControl.streamId());
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
        if (msId != RTMPConnection.CONTROL_MSID) {
            ServerNetStream stream = this.streams.get(msId);
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
            ServerNetStream stream = this.streams.get(msId);

            if (stream == null) {
                throw new CallError(NetStatus.NC_CALL_FAILED);
            }

            return stream.onCall(method, args);
        }

        switch (method) {
            case "connect": {
                ObjectLike res = this.connect(ConnectArgs.from(args));

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
