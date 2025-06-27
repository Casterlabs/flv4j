package co.casterlabs.flv4j.rtmp.net.rpc;

import co.casterlabs.flv4j.rtmp.net.NetStatus;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CallError extends Exception {
    private static final long serialVersionUID = 322008648945529734L;

    public final NetStatus status;

}
