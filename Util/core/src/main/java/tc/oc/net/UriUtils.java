package tc.oc.net;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;

public interface UriUtils {

    static String dataUri(String mime, byte[] data) {
        return "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(data);
    }

    static URL url(String spec) {
        try {
            return new URL(spec);
        } catch(MalformedURLException e) {
            throw new IllegalArgumentException("Malformed URL: " + spec);
        }
    }
}
