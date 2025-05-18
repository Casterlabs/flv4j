package co.casterlabs.flv4j.test.flv;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import co.casterlabs.commons.io.streams.StreamUtil;

class _Media {

    static {
        try {
            String licenseInfo = StreamUtil.toString(
                _Media.class.getClassLoader()
                    .getResourceAsStream("media/license.txt"),
                StandardCharsets.UTF_8
            );

            System.out.println(licenseInfo);
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();  // ?
        }
    }

    static InputStream stream() {
        return _Media.class
            .getClassLoader()
            .getResourceAsStream("media/bigbuckbunny_320x180_h264_aac.flv");
    }

    static byte[] bytes() throws IOException {
        return StreamUtil.toBytes(stream());
    }

}
