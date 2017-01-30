package tc.oc.commons.core.collection;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class CountingStringMap<V> extends ConflictResolvingMap<String, V> {

    private final BiMap<String, V> delegate;

    public CountingStringMap(int limit, String delimiter) {
        this(limit, delimiter, HashBiMap.create());
    }

    public CountingStringMap(int limit, String delimiter, BiMap<String, V> delegate) {
        super(limit, new StringIncrementer(delimiter));
        this.delegate = delegate;
    }

    @Override
    protected BiMap<String, V> delegate() {
        return delegate;
    }
}
