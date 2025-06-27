package co.casterlabs.flv4j.rtmp.net.server;

import java.io.IOException;

import co.casterlabs.flv4j.actionscript.amf0.AMF0Type;
import co.casterlabs.flv4j.actionscript.amf0.Boolean0;
import co.casterlabs.flv4j.actionscript.amf0.Null0;
import co.casterlabs.flv4j.actionscript.amf0.Number0;
import co.casterlabs.flv4j.actionscript.amf0.String0;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessage;
import co.casterlabs.flv4j.rtmp.net.CallError;
import co.casterlabs.flv4j.rtmp.net.NetStatus;
import co.casterlabs.flv4j.rtmp.net.NetStream;

public abstract class ServerNetStream extends NetStream {
    ServerNetConnection server;
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

            case "play": {
                String name = ((String0) args[1]).value();

                double start = -2; // default value per spec
                if (args.length > 2) {
                    start = ((Number0) args[2]).value();
                }

                double duration = -1; // default value per spec
                if (args.length > 3) {
                    start = ((Number0) args[3]).value();
                }

                boolean reset = true;
                if (args.length > 4) {
                    reset = ((Boolean0) args[4]).value();
                }

                this.play(name, start, duration, reset);
                return null;
            }

            case "receiveAudio": {
                boolean enabled = ((Boolean0) args[1]).value();

                this.receiveAudio(enabled);
                return null;
            }

            case "receiveVideo": {
                boolean enabled = ((Boolean0) args[1]).value();

                this.receiveVideo(enabled);
                return null;
            }

            case "publish": {
                String name = ((String0) args[1]).value();
                String type = ((String0) args[2]).value();

                this.publish(name, type);
                return null;
            }

            case "seek": {
                long milliseconds = (long) ((Number0) args[1]).value();

                this.seek(milliseconds);
                return null;
            }

            case "pause": {
                boolean pause = ((Boolean0) args[1]).value();
                long milliseconds = (long) ((Number0) args[2]).value();

                this.pause(pause, milliseconds);
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
    public final void sendMessage(int timestamp, RTMPMessage message) throws IOException, InterruptedException {
        if (this.id == 0 || this.server == null) {
            throw new IllegalStateException("Cannot send NetStream message before it has been initialized.");
        }

        this.server.conn.sendMessage(
            this.id,
            timestamp,
            message
        );
    }

    @Override
    public final void callVoid(String method, AMF0Type... args) throws IOException, InterruptedException {
        this.server.conn.callVoid(
            this.id,
            method,
            args
        );
    }

    @Override
    public final AMF0Type[] call(String method, AMF0Type... args) throws IOException, InterruptedException, CallError {
        return this.server.conn.call(
            this.id,
            method,
            args
        );
    }

}
