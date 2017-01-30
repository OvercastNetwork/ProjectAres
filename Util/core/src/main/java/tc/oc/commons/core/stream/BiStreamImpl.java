package tc.oc.commons.core.stream;

import java.util.Map;
import java.util.stream.Stream;

public class BiStreamImpl<K, V> extends ForwardingStream<Map.Entry<K, V>> implements BiStream<K, V> {
    public BiStreamImpl(Stream<Map.Entry<K, V>> delegate) {
        super(delegate);
    }
}
