package tc.oc.commons.core.collection;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import tc.oc.commons.core.util.SupersetView;

/**
 * Unmodifiable {@link Table} view of a {@link Map} of {@link Map}s
 *
 * Empty rows are ignored
 */
public class TableView<R, C, V> implements Table<R, C, V> {

    private final Map<R, Map<C, V>> map;

    public TableView(Map<R, Map<C, V>> map) {
        this.map = map;
    }

    @Override
    public boolean containsRow(@Nullable Object rowKey) {
        return map.containsKey(rowKey) && !map.get(rowKey).isEmpty();
    }

    @Override
    public boolean containsColumn(@Nullable Object columnKey) {
        return map.values().stream().anyMatch(row -> row.containsKey(columnKey));
    }

    @Override
    public boolean contains(@Nullable Object rowKey, @Nullable Object columnKey) {
        return map.containsKey(rowKey) && map.get(rowKey).containsKey(columnKey);
    }

    @Override
    public boolean containsValue(@Nullable Object value) {
        return map.values().stream().anyMatch(row -> row.values().stream().anyMatch(v -> Objects.equals(v, value)));
    }

    @Override
    public V get(@Nullable Object rowKey, @Nullable Object columnKey) {
        return map.containsKey(rowKey) ? map.get(rowKey).get(columnKey) : null;
    }

    @Override
    public boolean isEmpty() {
        return map.values().stream().allMatch(Map::isEmpty);
    }

    @Override
    public int size() {
        return map.values().stream().mapToInt(Map::size).sum();
    }

    @Override
    public Map<R, Map<C, V>> rowMap() {
        return map;
    }

    @Override
    public Map<C, V> row(R rowKey) {
        return map.get(rowKey);
    }

    @Override
    public Set<R> rowKeySet() {
        return Sets.filter(map.keySet(), this::containsRow);
    }

    @Override
    public Map<C, Map<R, V>> columnMap() {
        return Maps.asMap(columnKeySet(), this::column);
    }

    @Override
    public Map<R, V> column(C columnKey) {
        return Maps.asMap(Sets.filter(rowKeySet(), rowKey -> contains(rowKey, columnKey)),
                          rowKey -> map.get(rowKey).get(columnKey));
    }

    @Override
    public Set<C> columnKeySet() {
        return new SupersetView<>(Collections2.transform(map.values(), Map::keySet));
    }

    @Override
    public Collection<V> values() {
        return new FlatCollection<>(Collections2.transform(map.values(), Map::values));
    }

    @Override
    public Set<Cell<R, C, V>> cellSet() {
        return new AbstractSet<Cell<R, C, V>>() {
            @Override
            public int size() {
                return TableView.this.size();
            }

            @Override
            public boolean isEmpty() {
                return TableView.this.isEmpty();
            }

            @Override
            public boolean contains(Object o) {
                if(!(o instanceof Cell)) return false;
                final Cell cell = (Cell) o;
                return Objects.equals(get(cell.getRowKey(), cell.getColumnKey()), cell.getValue());
            }

            @Override
            public Stream<Cell<R, C, V>> stream() {
                return map.entrySet()
                          .stream()
                          .flatMap(row -> row.getValue()
                                             .entrySet()
                                             .stream()
                                             .map(col -> Tables.immutableCell(row.getKey(),
                                                                              col.getKey(),
                                                                              col.getValue())));
            }

            @Override
            public Spliterator<Cell<R, C, V>> spliterator() {
                return stream().spliterator();
            }

            @Override
            public Iterator<Cell<R, C, V>> iterator() {
                return Spliterators.iterator(spliterator());
            }
        };
    }

    @Override
    public void clear() {
        unsupported();
    }

    @Override
    public V put(R rowKey, C columnKey, V value) {
        throw unsupported();
    }

    @Override
    public void putAll(Table<? extends R, ? extends C, ? extends V> table) {
        unsupported();
    }

    @Override
    public V remove(@Nullable Object rowKey, @Nullable Object columnKey) {
        throw unsupported();
    }

    private UnsupportedOperationException unsupported() {
        throw new UnsupportedOperationException("This object is immutable");
    }
}
