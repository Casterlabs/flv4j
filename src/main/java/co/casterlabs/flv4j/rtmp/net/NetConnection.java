package co.casterlabs.flv4j.rtmp.net;

import java.io.IOException;

import co.casterlabs.flv4j.actionscript.amf0.AMF0Type;
import co.casterlabs.flv4j.actionscript.amf0.AMF0Type.ObjectLike;
import co.casterlabs.flv4j.actionscript.amf0.Null0;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessage;

// https://rtmp.veriskope.com/docs/spec/#721netconnection-commands
public abstract class NetConnection extends RPCHandler {

    public abstract ObjectLike connect(ConnectArgs args) throws IOException, InterruptedException, CallError;

    /* ------------------------ */

    /**
     * @return the stream id
     */
    public final NetStream createStream() throws IOException, InterruptedException, CallError {
        return this.createStream(Null0.INSTANCE);
    }

    /**
     * @return the stream id
     */
    public abstract NetStream createStream(AMF0Type arg) throws IOException, InterruptedException, CallError;

    /* ------------------------ */

    public void sendMessage(int timestamp, RTMPMessage message) throws IOException, InterruptedException {}

}
