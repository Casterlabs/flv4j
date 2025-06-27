package co.casterlabs.flv4j.rtmp.net;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import co.casterlabs.flv4j.actionscript.amf0.AMF0Type;
import co.casterlabs.flv4j.actionscript.amf0.AMF0Type.ObjectLike;
import co.casterlabs.flv4j.actionscript.amf0.AMF0Type.StringLike;
import co.casterlabs.flv4j.actionscript.amf0.Number0;
import co.casterlabs.flv4j.actionscript.amf0.Object0;
import co.casterlabs.flv4j.actionscript.amf0.StrictArray0;
import co.casterlabs.flv4j.actionscript.amf0.String0;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

// https://rtmp.veriskope.com/docs/spec/#7211connect
// https://veovera.org/docs/enhanced/enhanced-rtmp-v2#enhancing-netconnection-connect-command
@Data
@ToString
@Accessors(fluent = true, chain = true)
public class ConnectArgs {
    public static final int SUPPORT_SND_NONE = 0x0001;
    public static final int SUPPORT_SND_ADPCM = 0x0002;
    public static final int SUPPORT_SND_MP3 = 0x0004;
    public static final int SUPPORT_SND_INTEL = 0x0008;
    public static final int SUPPORT_SND_UNUSED = 0x0010;
    public static final int SUPPORT_SND_NELLY8 = 0x0020;
    public static final int SUPPORT_SND_NELLY = 0x0040;
    public static final int SUPPORT_SND_G711A = 0x0080;
    public static final int SUPPORT_SND_G711U = 0x0100;
    public static final int SUPPORT_SND_NELLY16 = 0x0200;
    public static final int SUPPORT_SND_AAC = 0x0400;
    public static final int SUPPORT_SND_SPEEX = 0x0800;
    public static final int SUPPORT_SND_ALL = 0x0FFF;

    public static final int SUPPORT_VID_UNUSED = 0x0001;
    public static final int SUPPORT_VID_JPEG = 0x0002;
    public static final int SUPPORT_VID_SORENSON = 0x0004;
    public static final int SUPPORT_VID_HOMEBREW = 0x0008;
    public static final int SUPPORT_VID_VP6 = 0x0010;
    public static final int SUPPORT_VID_VP6ALPHA = 0x0020;
    public static final int SUPPORT_VID_HOMEBREWV = 0x0040;
    public static final int SUPPORT_VID_H264 = 0x0080;
    public static final int SUPPORT_VID_ALL = 0x00FF;

    /**
     * Indicates that the client can perform frame-accurate seeks.
     */
    public static final int SUPPORT_VID_CLIENT_SEEK = 1;

    public static final int CAPS_EX_RECONNECT = 0x01;
    public static final int CAPS_EX_MULTITRACK = 0x02;
    public static final int CAPS_EX_MOD_EX = 0x04;
    public static final int CAPS_EX_TIMESTAMP_NANO_OFFSET = 0x04;

    public static final int FOUR_CC_INFO_DECODE = 0x01;
    public static final int FOUR_CC_INFO_ENCODE = 0x02;
    public static final int FOUR_CC_INFO_FORWARD = 0x03;

    private String app;
    private String flashVersion;
    private String swfUrl;
    private String tcUrl;
    private String pageUrl;

    private int audioCodecs = 0;
    private int videoCodecs = 0;
    private int videoFunction = 0;

    private int objectEncoding = 0;

    private int capsEx = 0;

    private String[] legacyFourCcList = {};

    private final Map<String, Integer> videoFourCcInfoMap = new HashMap<>();
    private final Map<String, Integer> audioFourCcInfoMap = new HashMap<>();
    // the legacy fourCcList is created dynamically :^)

    private AMF0Type[] optionalArgs = {};

    public ConnectArgs legacyFourCcList(String[] codecs) {
        throw new UnsupportedOperationException("Legacy fourCcList is generated automatically from videoFourCcInfoMap/audioFourCcInfoMap, thus this property is read-only.");
    }

    /* ------------------------ */

    public ConnectArgs canDecodeVideo(String codec, boolean can) {
        int mask = this.videoFourCcInfoMap.getOrDefault(codec, 0);
        if (can) {
            mask |= FOUR_CC_INFO_DECODE;
        } else {
            mask &= ~FOUR_CC_INFO_DECODE;
        }
        this.videoFourCcInfoMap.put(codec, mask);
        return this;
    }

    public boolean canDecodeVideo(String codec) {
        int mask = this.videoFourCcInfoMap.getOrDefault(codec, 0);
        return (mask & FOUR_CC_INFO_DECODE) == FOUR_CC_INFO_DECODE;
    }

    public ConnectArgs canEncodeVideo(String codec, boolean can) {
        int mask = this.videoFourCcInfoMap.getOrDefault(codec, 0);
        if (can) {
            mask |= FOUR_CC_INFO_ENCODE;
        } else {
            mask &= ~FOUR_CC_INFO_ENCODE;
        }
        this.videoFourCcInfoMap.put(codec, mask);
        return this;
    }

    public boolean canEncodeVideo(String codec) {
        int mask = this.videoFourCcInfoMap.getOrDefault(codec, 0);
        return (mask & FOUR_CC_INFO_ENCODE) == FOUR_CC_INFO_ENCODE;
    }

    public ConnectArgs canForwardVideo(String codec, boolean can) {
        int mask = this.videoFourCcInfoMap.getOrDefault(codec, 0);
        if (can) {
            mask |= FOUR_CC_INFO_FORWARD;
        } else {
            mask &= ~FOUR_CC_INFO_FORWARD;
        }
        this.videoFourCcInfoMap.put(codec, mask);
        return this;
    }

    public boolean canForwardVideo(String codec) {
        int mask = this.videoFourCcInfoMap.getOrDefault(codec, 0);
        return (mask & FOUR_CC_INFO_FORWARD) == FOUR_CC_INFO_FORWARD;
    }

    public ConnectArgs canDecodeAudio(String codec, boolean can) {
        int mask = this.audioFourCcInfoMap.getOrDefault(codec, 0);
        if (can) {
            mask |= FOUR_CC_INFO_DECODE;
        } else {
            mask &= ~FOUR_CC_INFO_DECODE;
        }
        this.audioFourCcInfoMap.put(codec, mask);
        return this;
    }

    public boolean canDecodeAudio(String codec) {
        int mask = this.audioFourCcInfoMap.getOrDefault(codec, 0);
        return (mask & FOUR_CC_INFO_DECODE) == FOUR_CC_INFO_DECODE;
    }

    public ConnectArgs canEncodeAudio(String codec, boolean can) {
        int mask = this.audioFourCcInfoMap.getOrDefault(codec, 0);
        if (can) {
            mask |= FOUR_CC_INFO_ENCODE;
        } else {
            mask &= ~FOUR_CC_INFO_ENCODE;
        }
        this.audioFourCcInfoMap.put(codec, mask);
        return this;
    }

    public boolean canEncodeAudio(String codec) {
        int mask = this.audioFourCcInfoMap.getOrDefault(codec, 0);
        return (mask & FOUR_CC_INFO_ENCODE) == FOUR_CC_INFO_ENCODE;
    }

    public ConnectArgs canForwardAudio(String codec, boolean can) {
        int mask = this.audioFourCcInfoMap.getOrDefault(codec, 0);
        if (can) {
            mask |= FOUR_CC_INFO_FORWARD;
        } else {
            mask &= ~FOUR_CC_INFO_FORWARD;
        }
        this.audioFourCcInfoMap.put(codec, mask);
        return this;
    }

    public boolean canForwardAudio(String codec) {
        int mask = this.audioFourCcInfoMap.getOrDefault(codec, 0);
        return (mask & FOUR_CC_INFO_FORWARD) == FOUR_CC_INFO_FORWARD;
    }

    /* ------------------------ */

    private Object0 toObject() {
        Map<String, AMF0Type> map = new HashMap<>();

        if (this.app != null) map.put("app", new String0(this.app));
        if (this.flashVersion != null) map.put("flashVersion", new String0(this.flashVersion));
        if (this.swfUrl != null) map.put("swfUrl", new String0(this.swfUrl));
        if (this.tcUrl != null) map.put("tcUrl", new String0(this.tcUrl));
        if (this.pageUrl != null) map.put("pageUrl", new String0(this.pageUrl));

        map.put("audioCodecs", new Number0(this.audioCodecs));
        map.put("videoCodecs", new Number0(this.videoCodecs));
        map.put("videoFunction", new Number0(this.videoFunction));
        map.put("objectEncoding", new Number0(this.objectEncoding));

        map.put("capsEx", new Number0(this.capsEx));

        {
            List<String0> legacyFourCcList = new LinkedList<>();
            Map<String, AMF0Type> videoFourCcMap = new HashMap<>();
            Map<String, AMF0Type> audioFourCcMap = new HashMap<>();

            for (Map.Entry<String, Integer> entry : this.videoFourCcInfoMap.entrySet()) {
                String codec = entry.getKey();
                int mask = entry.getValue();
                if (mask == 0) continue;

                legacyFourCcList.add(new String0(codec));
                videoFourCcMap.put(codec, new Number0(mask));
            }

            for (Map.Entry<String, Integer> entry : this.audioFourCcInfoMap.entrySet()) {
                String codec = entry.getKey();
                int mask = entry.getValue();
                if (mask == 0) continue;

                legacyFourCcList.add(new String0(codec));
                audioFourCcMap.put(codec, new Number0(mask));
            }

            map.put("videoFourCcInfoMap", new Object0(videoFourCcMap));
            map.put("audioFourCcInfoMap", new Object0(audioFourCcMap));
            map.put("fourCcList", new StrictArray0(legacyFourCcList.toArray(new String0[0])));
        }

        return new Object0(map);
    }

    public AMF0Type[] toAMF0() {
        AMF0Type[] arr = new AMF0Type[this.optionalArgs.length + 1];
        arr[0] = this.toObject();
        System.arraycopy(this.optionalArgs, 0, arr, 1, this.optionalArgs.length);
        return arr;
    }

    public static ConnectArgs from(AMF0Type... args) {
        Map<String, AMF0Type> map = objMap(args[0]);

        AMF0Type[] optional = {};
        if (args.length > 1) {
            optional = Arrays.copyOfRange(args, 1, args.length);
        }

        return from(map, optional);
    }

    private static ConnectArgs from(Map<String, AMF0Type> map, AMF0Type... optional) {
        ConnectArgs args = new ConnectArgs();
        args.optionalArgs = optional;

        if (map.containsKey("app")) args.app = ((StringLike) map.get("app")).value();
        if (map.containsKey("flashVersion")) args.flashVersion = ((StringLike) map.get("flashVersion")).value();
        if (map.containsKey("swfUrl")) args.swfUrl = ((StringLike) map.get("swfUrl")).value();
        if (map.containsKey("tcUrl")) args.tcUrl = ((StringLike) map.get("tcUrl")).value();
        if (map.containsKey("pageUrl")) args.pageUrl = ((StringLike) map.get("pageUrl")).value();

        if (map.containsKey("audioCodecs")) args.audioCodecs = (int) ((Number0) map.get("audioCodecs")).value();
        if (map.containsKey("videoCodecs")) args.videoCodecs = (int) ((Number0) map.get("videoCodecs")).value();
        if (map.containsKey("videoFunction")) args.videoFunction = (int) ((Number0) map.get("videoFunction")).value();
        if (map.containsKey("objectEncoding")) args.objectEncoding = (int) ((Number0) map.get("objectEncoding")).value();

        if (map.containsKey("capsEx")) args.capsEx = (int) ((Number0) map.get("capsEx")).value();

        if (map.containsKey("fourCcList")) {
            AMF0Type[] legacyFourCcList = ((StrictArray0) map.get("fourCcList")).array();
            args.legacyFourCcList = new String[legacyFourCcList.length];
            for (int i = 0; i < legacyFourCcList.length; i++) {
                args.legacyFourCcList[i] = ((StringLike) legacyFourCcList[i]).value();
            }
        }

        if (map.containsKey("videoFourCcInfoMap")) {
            Map<String, AMF0Type> videoFourCcMap = objMap(map.get("videoFourCcInfoMap"));

            for (Map.Entry<String, AMF0Type> entry : videoFourCcMap.entrySet()) {
                args.videoFourCcInfoMap.put(entry.getKey(), (int) ((Number0) entry.getValue()).value());
            }
        }

        if (map.containsKey("audioFourCcInfoMap")) {
            Map<String, AMF0Type> audioFourCcMap = objMap(map.get("audioFourCcInfoMap"));

            for (Map.Entry<String, AMF0Type> entry : audioFourCcMap.entrySet()) {
                args.audioFourCcInfoMap.put(entry.getKey(), (int) ((Number0) entry.getValue()).value());
            }
        }

        return args;
    }

    private static Map<String, AMF0Type> objMap(AMF0Type type) {
        if (type instanceof ObjectLike obj) {
            return obj.map();
        } else {
            throw new IllegalArgumentException("Invalid type: " + type);
        }
    }

}
