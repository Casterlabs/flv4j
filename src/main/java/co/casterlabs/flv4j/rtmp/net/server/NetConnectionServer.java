package co.casterlabs.flv4j.rtmp.net.server;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import co.casterlabs.flv4j.actionscript.amf0.AMF0Type;
import co.casterlabs.flv4j.actionscript.amf0.Null0;
import co.casterlabs.flv4j.actionscript.amf0.Number0;
import co.casterlabs.flv4j.actionscript.amf0.Object0;
import co.casterlabs.flv4j.actionscript.amf0.String0;
import co.casterlabs.flv4j.rtmp.RTMPReader;
import co.casterlabs.flv4j.rtmp.RTMPWriter;
import co.casterlabs.flv4j.rtmp.chunks.RTMPChunk;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessage;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessageAcknowledgement;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessageChunkSize;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessageCommand0;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessageSetPeerBandwidth;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessageSetPeerBandwidth.LimitType;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessageUserControl;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessageWindowAcknowledgementSize;
import co.casterlabs.flv4j.rtmp.chunks.control.RTMPStreamBeginControlMessage;
import co.casterlabs.flv4j.rtmp.handshake.RTMPHandshake1;
import co.casterlabs.flv4j.rtmp.handshake.RTMPHandshake2;
import co.casterlabs.flv4j.rtmp.net.CallError;
import co.casterlabs.flv4j.rtmp.net.NetConnection;
import co.casterlabs.flv4j.rtmp.net.NetStatus;
import lombok.SneakyThrows;

public abstract class NetConnectionServer extends NetConnection {
    private static final int CHUNK_SIZE = 4096;
    private static final int WINDOW_ACK_SIZE = 2500000;

    private final RTMPReader in;
    private final RTMPWriter out;

    Map<Integer, Future<AMF0Type[]>> rpcFutures = new HashMap<>();
    AtomicInteger currTsId = new AtomicInteger(1); // 0 is reserved.

    private AtomicInteger currStreamId = new AtomicInteger(1); // 0 is reserved.
    Map<Integer, ServerNetStream> streams = new HashMap<>();

    public NetConnectionServer(RTMPReader in, RTMPWriter out) {
        this.in = in;
        this.out = out;
    }

    public void run() throws IOException, InterruptedException {
        this.in.handshake0(); // Consume. Should always be version 3.
        this.out.handshake0();

        RTMPHandshake1 handshake1 = this.in.handshake1();
        this.out.handshake1();

        this.out.handshake2(handshake1);
        RTMPHandshake2 handshake2 = this.in.handshake2();

        if (!this.out.validateHandshake2(handshake2)) {
            throw new IOException("Handshake failed!");
        }

        try {
            while (true) {
                if (this.in.needsAck()) {
                    this.sendMessage(CONTROL_MSID, 0, new RTMPMessageAcknowledgement(this.in.ackSeq()));
                }

                RTMPChunk<?> read = this.in.read();
                if (read == null) continue;

                int msId = (int) read.messageStreamId();
                RTMPMessage message = read.message();

                if (message instanceof RTMPMessageCommand0 command) {
                    AMF0Type[] args = command.arguments().toArray(new AMF0Type[0]);

                    try {
                        AMF0Type[] response = this.onCall(msId, command.commandName().value(), args);
                        if (command.transactionId().value() == 0) continue; // void.

                        if (response == null) {
                            response = new AMF0Type[] {
                                    Null0.INSTANCE
                            };
                        }

                        this.sendMessage(
                            msId,
                            0, // ?
                            new RTMPMessageCommand0(
                                _RESULT,
                                command.transactionId(),
                                Arrays.asList(response)
                            )
                        );
                    } catch (CallError e) {
                        if (command.transactionId().value() == 0) continue; // void

                        this.sendMessage(
                            msId,
                            0, // ?
                            new RTMPMessageCommand0(
                                _ERROR,
                                command.transactionId(),
                                Arrays.asList(e.status.asObject())
                            )
                        );
                    }
                } else {
                    if (msId == CONTROL_MSID) {
                        if (this.onMessage == null) {
                            continue; // DROP.
                        }

                        this.onMessage.onMessage(msId, read.timestamp(), message);
                    } else {
                        ServerNetStream stream = this.streams.get(msId);

                        if (stream == null || stream.onMessage == null) {
                            continue; // DROP.
                        }

                        stream.onMessage.onMessage(msId, read.timestamp(), message);
                    }
                }
            }
        } finally {
            for (ServerNetStream stream : this.streams.values()) {
                try {
                    stream.deleteStream();
                } catch (IOException | InterruptedException | CallError ignored) {}
            }
        }
    }

    public int activeStreams() {
        return this.streams.size();
    }

    /* ------------------------ */

    void sendMessage(int msId, int timestamp, RTMPMessage message) throws IOException, InterruptedException {
        this.out.write(
            msId,
            timestamp,
            message
        );
    }

    private final AMF0Type[] onCall(int msId, String method, AMF0Type... args) throws IOException, InterruptedException, CallError {
        if (msId != CONTROL_MSID) {
            ServerNetStream stream = this.streams.get(msId);

            if (stream == null) {
                throw new CallError(NetStatus.NC_CALL_FAILED);
            }

            return stream.onCall(method, args);
        }

        switch (method) {
            case "connect": {
                Object0 res = this.connect(args);

                this.sendMessage(
                    0,
                    new RTMPMessageWindowAcknowledgementSize(WINDOW_ACK_SIZE)
                );
                this.sendMessage(
                    0,
                    new RTMPMessageSetPeerBandwidth(WINDOW_ACK_SIZE, LimitType.DYNAMIC.id)
                );
                this.in.setWindowAcknowledgementSize(WINDOW_ACK_SIZE);
                this.sendMessage(
                    0,
                    new RTMPMessageChunkSize(CHUNK_SIZE)
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
                    return this.onCall.onCall(msId, method, args);
                }
        }
    }

    @Override
    public abstract ServerNetStream createStream(AMF0Type arg) throws IOException, InterruptedException, CallError;

    /* ------------------------ */

    @Override
    public void sendMessage(int timestamp, RTMPMessage message) throws IOException, InterruptedException {
        this.sendMessage(
            CONTROL_MSID,
            timestamp,
            message
        );
    }

    @Override
    public void callVoid(String method, AMF0Type... args) throws IOException, InterruptedException {
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
    public AMF0Type[] call(String method, AMF0Type... args) throws IOException, InterruptedException, CallError {
        CompletableFuture<AMF0Type[]> future = new CompletableFuture<>();

        int tsId = this.currTsId.getAndIncrement();
        this.rpcFutures.put(tsId, future);

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
