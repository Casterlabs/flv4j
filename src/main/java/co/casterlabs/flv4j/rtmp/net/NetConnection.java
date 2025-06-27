package co.casterlabs.flv4j.rtmp.net;

import java.io.IOException;
import java.util.List;

import co.casterlabs.flv4j.actionscript.amf0.AMF0Type.ObjectLike;
import co.casterlabs.flv4j.rtmp.chunks.RTMPMessage;
import co.casterlabs.flv4j.rtmp.net.rpc.CallError;
import co.casterlabs.flv4j.rtmp.net.rpc.RPCHandler;

// https://rtmp.veriskope.com/docs/spec/#721netconnection-commands
public abstract class NetConnection extends RPCHandler {

    public abstract ObjectLike connect(ConnectArgs args) throws IOException, InterruptedException, CallError;

    public abstract List<NetStream> streams();

    public abstract void sendMessage(int timestamp, RTMPMessage message) throws IOException, InterruptedException;

}
