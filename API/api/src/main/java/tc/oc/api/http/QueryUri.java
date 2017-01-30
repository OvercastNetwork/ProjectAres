package tc.oc.api.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.google.api.client.repackaged.com.google.common.base.Joiner;
import com.google.common.base.Charsets;

public class QueryUri {

    private static final Joiner JOINER = Joiner.on('&');

    private final String prefix;
    private final List<String> vars = new ArrayList<>();

    public QueryUri(String prefix) {
        this.prefix = prefix;
    }

    public QueryUri put(String name, Object value) {
        try {
            vars.add(URLEncoder.encode(name, Charsets.UTF_8.name()) +
                     "=" +
                     URLEncoder.encode(String.valueOf(value), Charsets.UTF_8.name()));
        } catch(UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
        return this;
    }

    public String encode() {
        if(vars.isEmpty()) {
            return prefix;
        } else {
            return prefix + '?' + JOINER.join(vars);
        }
    }
}
