package co.casterlabs.flv4j.test.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import co.casterlabs.commons.io.streams.StreamUtil;

public class Media {

    static {
        try {
            String licenseInfo = StreamUtil.toString(
                Media.class.getClassLoader()
                    .getResourceAsStream("media/license.txt"),
                StandardCharsets.UTF_8
            );

            System.out.println(licenseInfo);
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();  // ?
        }
    }

    public static InputStream stream() {
        return Media.class
            .getClassLoader()
            .getResourceAsStream("media/bigbuckbunny_320x180_h264_aac.flv");
    }

    public static byte[] bytes() throws IOException {
        return StreamUtil.toBytes(stream());
    }

}
