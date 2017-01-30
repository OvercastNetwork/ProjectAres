package tc.oc.commons.core.util;

import java.util.function.BiConsumer;

import com.google.common.collect.Multimap;

public interface MultimapHelper<K, V> extends Multimap<K, V> {

    default void forEach(BiConsumer<K, V> block) { forEach(this, block); }
    static <K, V> void forEach(Multimap<K, V> multimap, BiConsumer<K, V> block) {
        multimap.asMap().forEach((key, values) -> {
            values.forEach(value -> block.accept(key, value));
        });
    }
}
