package co.casterlabs.flv4j.rtmp.net;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.flv4j.actionscript.amf0.AMF0Type;
import co.casterlabs.flv4j.actionscript.amf0.ECMAArray0;
import co.casterlabs.flv4j.actionscript.amf0.Null0;
import co.casterlabs.flv4j.actionscript.amf0.Number0;
import co.casterlabs.flv4j.actionscript.amf0.Object0;
import co.casterlabs.flv4j.actionscript.amf0.String0;
import co.casterlabs.flv4j.rtmp.RTMPReader;
import co.casterlabs.flv4j.rtmp.RTMPWriter;
import co.casterlabs.flv4j.rtmp.chunks.RTMPChunk;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessage;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessageAcknowledgement;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessageCommand0;
import co.casterlabs.flv4j.rtmp.handshake.RTMPHandshake1;
import co.casterlabs.flv4j.rtmp.handshake.RTMPHandshake2;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class RTMPConnection {
    public static final int CONTROL_MSID = 0;

    private static final Number0 VOID_TSID = new Number0(0);
    private static final String0 _RESULT = new String0("_result");
    private static final String0 _ERROR = new String0("_error");

    private final Map<Integer, CompletableFuture<AMF0Type[]>> rpcFutures = new HashMap<>();
    private final AtomicInteger currTsId = new AtomicInteger(1); // 0 is reserved for void calls.

    private final RTMPReader in;
    private final RTMPWriter out;

    public @Nullable ConnCallHandler onCall;
    public @Nullable ConnMessageHandler onMessage;

    public void setWindowAcknowledgementSize(int size) {
        this.in.setWindowAcknowledgementSize(size);
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
                        // We handle these specially for RPC calls :^)
                        switch (command.commandName().value()) {
                            case "_result": {
                                CompletableFuture<AMF0Type[]> future = this.rpcFutures.remove(msId);
                                if (future == null) continue;
                                future.complete(args);
                                continue; // we do not respond.
                            }

                            case "_error": {
                                CompletableFuture<AMF0Type[]> future = this.rpcFutures.remove(msId);
                                if (future == null) continue;

                                NetStatus status;
                                if (args[0] instanceof Object0 obj) {
                                    status = new NetStatus(obj);
                                } else if (args[0] instanceof ECMAArray0 arr) {
                                    status = new NetStatus(arr);
                                } else {
                                    throw new IllegalArgumentException("Invalid error reply: " + Arrays.toString(args));
                                }

                                future.completeExceptionally(new CallError(status));
                                continue; // we do not respond.
                            }

                            default:
                                break; // fall through
                        }

                        AMF0Type[] response = null;
                        if (this.onCall != null) {
                            response = this.onCall.onCall(msId, command.commandName().value(), args);
                        }

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
                    if (this.onMessage == null) {
                        continue; // DROP.
                    }

                    this.onMessage.onMessage(msId, read.timestamp(), message);
                }
            }
        } catch (IOException | InterruptedException e) {
            this.rpcFutures.forEach((k, v) -> v.completeExceptionally(e));
            throw e;
        }
    }

    public void sendMessage(int msId, int timestamp, RTMPMessage message) throws IOException, InterruptedException {
        this.out.write(
            msId,
            timestamp,
            message
        );
    }

    public void callVoid(int msId, String method, AMF0Type... args) throws IOException, InterruptedException {
        this.sendMessage(
            msId,
            0, // ?
            new RTMPMessageCommand0(
                new String0(method),
                VOID_TSID,
                Arrays.asList(args)
            )
        );
    }

    @SneakyThrows
    public AMF0Type[] call(int msId, String method, AMF0Type... args) throws IOException, InterruptedException, CallError {
        CompletableFuture<AMF0Type[]> future = new CompletableFuture<>();

        int tsId = this.currTsId.getAndIncrement();
        this.rpcFutures.put(tsId, future);

        this.sendMessage(
            msId,
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
            throw e.getCause(); // sneakythrows lets us wrap this :^)
        }
    }

    @FunctionalInterface
    public interface ConnCallHandler {
        public @Nullable AMF0Type[] onCall(int msId, String method, AMF0Type... args) throws IOException, InterruptedException, CallError;
    }

    @FunctionalInterface
    public interface ConnMessageHandler {
        public void onMessage(int msId, int timestamp, RTMPMessage message);
    }

}
