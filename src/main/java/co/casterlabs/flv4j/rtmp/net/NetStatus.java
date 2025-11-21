package co.casterlabs.flv4j.rtmp.net;

import java.util.Map;

import co.casterlabs.flv4j.actionscript.amf0.AMF0Type.ObjectLike;
import co.casterlabs.flv4j.actionscript.amf0.AMF0Type.StringLike;
import co.casterlabs.flv4j.actionscript.amf0.Object0;
import co.casterlabs.flv4j.actionscript.amf0.String0;
import co.casterlabs.flv4j.actionscript.io.ByteString;
import lombok.NonNull;

//https://veovera.org/docs/enhanced/enhanced-rtmp-v2.pdf#page=20
//https://helpx.adobe.com/adobe-media-server/ssaslr/netstream-class.html
//https://helpx.adobe.com/adobe-media-server/ssaslr/netconnection-class.html

public record NetStatus(
    String code,
    String level,
    String description,
    Object0 asObject
) {

    // @formatter:off
    public static final NetStatus NC_CONNECT_APP_SHUTDOWN      = new NetStatus("NetConnection.Connect.AppShutdown",      "error",  "Server going away.");
    public static final NetStatus NC_CONNECT_CLOSED            = new NetStatus("NetConnection.Connect.Closed",           "status", "Connection closed.");
    public static final NetStatus NC_CONNECT_FAILED            = new NetStatus("NetConnection.Connect.Failed",           "error",  "Connection failed.");
    public static final NetStatus NC_CONNECT_REJECTED          = new NetStatus("NetConnection.Connect.Rejected",         "error",  "Connection rejected.");
    public static final NetStatus NC_CONNECT_SUCCESS           = new NetStatus("NetConnection.Connect.Success",          "status", "Connection accepted.");
    public static final NetStatus NC_CONNECT_RECONNECT_REQUEST = new NetStatus("NetConnection.Connect.ReconnectRequest", "status", "Reconnection requested.");
    
    public static final NetStatus NC_CALL_FAILED               = new NetStatus("NetConnection.Call.Failed",              "error",  "Unable to invoke RPC command.");
    
    public static final NetStatus NS_CONNECT_SUCCESS           = new NetStatus("NetStream.Connect.Success",              "status", "Stream created.");
    public static final NetStatus NS_CONNECT_FAILED            = new NetStatus("NetStream.Connect.Failed",               "error",  "Failed to create stream.");
    
    public static final NetStatus NS_PLAY_FAILED               = new NetStatus("NetStream.Play.Failed",                  "error",  "Failed to play stream.");
    
    public static final NetStatus NS_PUBLISH_BADNAME           = new NetStatus("NetStream.Publish.BadName",              "error",  "Stream is already in use.");
    public static final NetStatus NS_PUBLISH_FAILED            = new NetStatus("NetStream.Publish.Failed",               "error",  "Failed to publish stream.");
    public static final NetStatus NS_PUBLISH_START             = new NetStatus("NetStream.Publish.Start",                "status", "Stream started.");

    public static final NetStatus NS_UNPUBLISH_SUCCESS         = new NetStatus("NetStream.Unpublish.Success",            "status", "Stream stopped.");
    // @formatter:on

    static final ByteString STR_CODE = new ByteString("code");
    static final ByteString STR_LEVEL = new ByteString("level");
    static final ByteString STR_DESCRIPTION = new ByteString("description");

    public NetStatus(@NonNull String code, @NonNull String level, @NonNull String description) {
        this(
            code, level, description, new Object0(
                Map.of(
                    STR_CODE, new String0(code),
                    STR_LEVEL, new String0(level),
                    STR_DESCRIPTION, new String0(description)
                )
            )
        );
    }

    public NetStatus(ObjectLike obj) {
        this(
            ((StringLike) obj.map().getOrDefault(STR_CODE, String0.EMPTY)).value().string(),
            ((StringLike) obj.map().getOrDefault(STR_LEVEL, String0.EMPTY)).value().string(),
            ((StringLike) obj.map().getOrDefault(STR_DESCRIPTION, String0.EMPTY)).value().string(),
            obj instanceof Object0 ? (Object0) obj : new Object0(obj.map())
        );
    }

}
