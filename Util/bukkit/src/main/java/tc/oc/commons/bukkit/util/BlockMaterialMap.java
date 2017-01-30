package tc.oc.commons.bukkit.util;

import java.lang.reflect.Array;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import gnu.trove.impl.Constants;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TIterator;
import gnu.trove.iterator.TLongIntIterator;
import gnu.trove.map.TLongIntMap;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.procedure.TLongIntProcedure;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.material.MaterialData;
import org.bukkit.util.BlockVector;

import static tc.oc.commons.bukkit.util.BlockUtils.decodePos;
import static tc.oc.commons.bukkit.util.BlockUtils.encodePos;
import static tc.oc.commons.bukkit.util.MaterialUtils.decodeMaterial;
import static tc.oc.commons.bukkit.util.MaterialUtils.decodeMetadata;
import static tc.oc.commons.bukkit.util.MaterialUtils.decodeTypeId;
import static tc.oc.commons.bukkit.util.MaterialUtils.encodeMaterial;
import static tc.oc.commons.bukkit.util.MaterialUtils.encodeMaterialSet;

/**
 * A map of {@link BlockVector} to {@link MaterialData}, implemented entirely with
 * primitive collections, so key/value objects are never created except when calling
 * a method that returns them. This container can be used to represent an arbitrary
 * set of block states in a very efficient way, though tile entities cannot be stored.
 *
 * The methods used to encode and decode contained data can be found
 * in {@link BlockUtils} and {@link MaterialUtils}.
 */
public class BlockMaterialMap implements Map<BlockVector, MaterialData> {

    public static final long NO_KEY = BlockUtils.ENCODED_NULL_POS;
    public static final int NO_VALUE = MaterialUtils.ENCODED_NULL_MATERIAL;

    private final TLongIntMap map;

    public BlockMaterialMap(TLongIntMap map) {
        this.map = map;
    }

    public BlockMaterialMap(int capacity) {
        this(new TLongIntHashMap(capacity, Constants.DEFAULT_LOAD_FACTOR, NO_KEY, NO_VALUE));
    }

    public BlockMaterialMap() {
        this(Constants.DEFAULT_CAPACITY);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean containsKey(long encodedPos) {
        return map.containsKey(encodedPos);
    }

    public boolean containsKey(int x, int y, int z) {
        return map.containsKey(encodePos(x, y, z));
    }

    public boolean containsKey(BlockVector pos) {
        return containsKey(encodePos(pos));
    }

    @Override
    public boolean containsKey(Object pos) {
        return pos instanceof BlockVector && containsKey((BlockVector) pos);
    }

    public boolean containsValue(int encodedMaterial) {
        return map.containsValue(encodedMaterial);
    }

    public boolean containsValue(int typeId, byte metadata) {
        return containsValue(encodeMaterial(typeId, metadata));
    }

    public boolean containsValue(MaterialData material) {
        return containsValue(encodeMaterial(material));
    }

    @Override
    public boolean containsValue(Object material) {
        return material instanceof MaterialData && containsValue((MaterialData) material);
    }

    public int getEncoded(long encodedPos) {
        return map.get(encodedPos);
    }

    public int getEncoded(int x, int y, int z) {
        return getEncoded(encodePos(x, y, z));
    }

    public int getEncoded(BlockVector pos) {
        return getEncoded(encodePos(pos));
    }

    public int getTypeId(long encodedPos) {
        return decodeTypeId(getEncoded(encodedPos));
    }

    public int getTypeId(int x, int y, int z) {
        return getTypeId(encodePos(x, y, z));
    }

    public int getTypeId(BlockVector pos) {
        return getTypeId(encodePos(pos));
    }

    public byte getMetadata(long encodedPos) {
        return decodeMetadata(getEncoded(encodedPos));
    }

    public byte getMetadata(int x, int y, int z) {
        return getMetadata(encodePos(x, y, z));
    }

    public byte getMetadata(BlockVector pos) {
        return getMetadata(encodePos(pos));
    }

    public MaterialData get(long encodedPos) {
        return decodeMaterial(getEncoded(encodedPos));
    }

    public MaterialData get(int x, int y, int z) {
        return get(encodePos(x, y, z));
    }

    public MaterialData get(BlockVector pos) {
        return get(encodePos(pos));
    }

    @Override
    public MaterialData get(Object pos) {
        return pos instanceof BlockVector ? get((BlockVector) pos) : null;
    }

    public int putEncoded(long encodedPos, int encodedMaterial) {
        return map.put(encodedPos, encodedMaterial);
    }

    public int putEncoded(int x, int y, int z, int encodedMaterial) {
        return putEncoded(encodePos(x, y, z), encodedMaterial);
    }

    public int putEncoded(BlockVector pos, int encodedMaterial) {
        return putEncoded(encodePos(pos), encodedMaterial);
    }

    public int putEncoded(long encodedPos, int typeId, byte metadata) {
        return putEncoded(encodedPos, encodeMaterial(typeId, metadata));
    }

    public int putEncoded(int x, int y, int z, int typeId, byte metadata) {
        return putEncoded(encodePos(x, y, z), typeId, metadata);
    }

    public int putEncoded(BlockVector pos, int typeId, byte metadata) {
        return putEncoded(encodePos(pos), typeId, metadata);
    }

    public int putEncoded(long encodedPos, MaterialData material) {
        return putEncoded(encodedPos, encodeMaterial(material));
    }

    public int putEncoded(int x, int y, int z, MaterialData material) {
        return putEncoded(encodePos(x, y, z), material);
    }

    public int putEncoded(BlockVector pos, MaterialData material) {
        return putEncoded(encodePos(pos), material);
    }

    public int putEncoded(BlockState state) {
        return putEncoded(encodePos(state.getX(), state.getY(), state.getZ()), encodeMaterial(state.getMaterialData()));
    }

    public MaterialData put(long encodedPos, int encodedMaterial) {
        return decodeMaterial(putEncoded(encodedPos, encodedMaterial));
    }

    public MaterialData put(int x, int y, int z, int encodedMaterial) {
        return decodeMaterial(putEncoded(x, y, z, encodedMaterial));
    }

    public MaterialData put(BlockVector pos, int encodedMaterial) {
        return decodeMaterial(putEncoded(pos, encodedMaterial));
    }

    public MaterialData put(long encodedPos, int typeId, byte metadata) {
        return decodeMaterial(putEncoded(encodedPos, typeId, metadata));
    }

    public MaterialData put(int x, int y, int z, int typeId, byte metadata) {
        return decodeMaterial(putEncoded(x, y, z, typeId, metadata));
    }

    public MaterialData put(BlockVector pos, int typeId, byte metadata) {
        return decodeMaterial(putEncoded(pos, typeId, metadata));
    }

    public MaterialData put(long encodedPos, MaterialData material) {
        return decodeMaterial(putEncoded(encodedPos, material));
    }

    public MaterialData put(int x, int y, int z, MaterialData material) {
        return decodeMaterial(putEncoded(x, y, z, material));
    }

    @Override
    public MaterialData put(BlockVector pos, MaterialData material) {
        return decodeMaterial(putEncoded(pos, material));
    }

    public MaterialData put(BlockState state) {
        return put(encodePos(state), encodeMaterial(state.getMaterialData()));
    }

    public int removeEncoded(long encodedPos) {
        return map.remove(encodedPos);
    }

    public int removeEncoded(int x, int y, int z) {
        return removeEncoded(encodePos(x, y, z));
    }

    public int removeEncoded(BlockVector pos) {
        return removeEncoded(encodePos(pos));
    }

    public MaterialData remove(long encodedPos) {
        return decodeMaterial(removeEncoded(encodedPos));
    }

    public MaterialData remove(int x, int y, int z) {
        return decodeMaterial(removeEncoded(x, y, z));
    }

    public MaterialData remove(BlockVector pos) {
        return decodeMaterial(removeEncoded(pos));
    }

    @Override
    public MaterialData remove(Object pos) {
        return pos instanceof BlockVector ? remove((BlockVector) pos) : null;
    }

    public boolean remove(BlockState state) {
        long encodedPos = encodePos(state);
        int encodedMaterial = getEncoded(encodedPos);
        if(encodedMaterial == encodeMaterial(state)) {
            removeEncoded(encodedPos);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public void putAll(Map<? extends BlockVector, ? extends MaterialData> m) {
        for(Entry<? extends BlockVector, ? extends MaterialData> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Set<BlockVector> keySet() {
        return new BlockVectorSet(map.keySet());
    }

    @Override
    public Collection<MaterialData> values() {
        return new ValueCollection();
    }

    @Override
    public Set<Entry<BlockVector, MaterialData>> entrySet() {
        return new EntrySet();
    }

    /**
     * Return a set of {@link BlockState}s backed by this map. The returned set is
     * writable and shares all state with the original container. Iterating over
     * the set is not recommended, as creating {@link BlockState}s from arbitrary
     * data is rather inefficient.
     */
    public Set<BlockState> asBlockStates(World world) {
        return new BlockStateSet(world);
    }

    public long getEncodedKeyAt(int n) {
        return map.keys()[n];
    }

    /**
     * Get the key at position N for some arbitrary ordering. The order is constant
     * as long as the state of the container does not change, but is otherwise
     * undefined.
     */
    public BlockVector getKeyAt(int n) {
        return decodePos(getEncodedKeyAt(n));
    }

    abstract class IteratorWrapper<T, I extends TIterator> implements Iterator<T> {
        final I iter;

        protected IteratorWrapper(I iter) {
            this.iter = iter;
        }

        @Override
        public boolean hasNext() {
            return iter.hasNext();
        }

        @Override
        public void remove() {
            iter.remove();
        }
    }

    class ValueIterator extends IteratorWrapper<MaterialData, TIntIterator> {
        public ValueIterator() {
            super(map.valueCollection().iterator());
        }

        @Override
        public MaterialData next() {
            return decodeMaterial(iter.next());
        }
    }

    class EntryIterator extends IteratorWrapper<Map.Entry<BlockVector, MaterialData>, TLongIntIterator> {
        protected EntryIterator() {
            super(map.iterator());
        }

        @Override
        public Entry<BlockVector, MaterialData> next() {
            iter.advance();
            return new AbstractMap.SimpleEntry<>(decodePos(iter.key()), decodeMaterial(iter.value()));
        }
    }

    class ValueCollection implements Collection<MaterialData> {
        @Override
        public int size() {
            return BlockMaterialMap.this.size();
        }

        @Override
        public boolean isEmpty() {
            return BlockMaterialMap.this.isEmpty();
        }

        @Override
        public boolean contains(Object material) {
            return BlockMaterialMap.this.containsValue(material);
        }

        @Override
        public Iterator<MaterialData> iterator() {
            return new ValueIterator();
        }

        @Override
        public Object[] toArray() {
            return toArray(new MaterialData[size()]);
        }

        @Override
        public <T> T[] toArray(T[] a) {
            Class<T> type = (Class<T>) a.getClass().getComponentType();

            if(size() <= a.length) {
                MaterialData[] materials = (MaterialData[]) a; // avoid casting every element
                int i = 0;
                for(MaterialData material : this) {
                    materials[i++] = material;
                }
                if(i < materials.length) materials[i] = null;
                return (T[]) materials;
            } else {
                return toArray((T[]) Array.newInstance(type, size()));
            }
        }

        @Override
        public boolean containsAll(Collection<?> materials) {
            return BlockMaterialMap.this.map.valueCollection().containsAll(encodeMaterialSet(materials));
        }

        @Override
        public boolean remove(Object material) {
            return material instanceof MaterialData && BlockMaterialMap.this.map.valueCollection().remove(encodeMaterial((MaterialData) material));
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return BlockMaterialMap.this.map.valueCollection().removeAll(encodeMaterialSet(c));
        }

        @Override
        public boolean add(MaterialData material) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(Collection<? extends MaterialData> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return BlockMaterialMap.this.map.valueCollection().retainAll(encodeMaterialSet(c));
        }

        @Override
        public void clear() {
            BlockMaterialMap.this.clear();
        }
    }

    abstract class SetView<T> implements Set<T> {
        @Override
        public int size() {
            return BlockMaterialMap.this.size();
        }

        @Override
        public boolean isEmpty() {
            return BlockMaterialMap.this.isEmpty();
        }

        abstract Class<?> entryArrayType();

        abstract T castEntry(Object obj);

        abstract long encodedKey(T entry);

        abstract int encodedValue(T entry);

        @Override
        public boolean contains(Object obj) {
            T entry = castEntry(obj);
            return entry != null && encodedKey(entry) == encodedValue(entry);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            for(Object o : c) {
                if(!contains(o)) return false;
            }
            return true;
        }

        @Override
        public Object[] toArray() {
            return toArray((T[]) Array.newInstance(entryArrayType(), size()));
        }

        @Override
        public <E> E[] toArray(E[] a) {
            Class<E> type = (Class<E>) a.getClass().getComponentType();

            if(size() <= a.length) {
                T[] entries = (T[]) entryArrayType().cast(a); // avoid casting every element
                int i = 0;
                for(T entry : this) {
                    entries[i++] = entry;
                }
                if(i < entries.length) entries[i] = null;
                return (E[]) entries;
            } else {
                return toArray((E[]) Array.newInstance(type, size()));
            }
        }

        @Override
        public boolean add(T entry) {
            int oldMaterial = putEncoded(encodedKey(entry), encodedValue(entry));
            return oldMaterial != encodedValue(entry);
        }

        @Override
        public boolean addAll(Collection<? extends T> c) {
            boolean changed = false;
            for(T entry : c) {
                if(add(entry)) changed = true;
            }
            return changed;
        }

        @Override
        public boolean remove(Object obj) {
            T entry = castEntry(obj);
            if(entry == null) return false;
            long encodedPos = encodedKey(entry);
            int encodedMaterial = getEncoded(encodedPos);
            if(encodedMaterial == encodedValue(entry)) {
                removeEncoded(encodedPos);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean removeAll(Collection<?> entries) {
            boolean changed = false;
            for(Object obj : entries) {
                if(remove(obj)) changed = true;
            }
            return changed;
        }

        @Override
        public boolean retainAll(final Collection<?> entries) {
            return map.retainEntries(new TLongIntProcedure() {
                @Override
                public boolean execute(long encodedPos, int encodedMaterial) {
                    for(Object obj : entries) {
                        T entry = castEntry(obj);
                        if(entry != null && encodedPos == encodedKey(entry) && encodedMaterial == encodedValue(entry)) {
                            return true;
                        }
                    }
                    return false;
                }
            });
        }

        @Override
        public void clear() {
            BlockMaterialMap.this.clear();
        }
    }

    class EntrySet extends SetView<Entry<BlockVector, MaterialData>> {

        @Override
        Class<?> entryArrayType() {
            return Entry[].class;
        }

        @Override
        Entry<BlockVector, MaterialData> castEntry(Object obj) {
            if(!(obj instanceof Entry)) return null;
            Entry entry = (Entry) obj;
            if(entry.getKey() instanceof BlockVector && entry.getValue() instanceof MaterialData) {
                return (Entry<BlockVector, MaterialData>) entry;
            } else {
                return null;
            }
        }

        @Override
        long encodedKey(Entry<BlockVector, MaterialData> entry) {
            return encodePos(entry.getKey());
        }

        @Override
        int encodedValue(Entry<BlockVector, MaterialData> entry) {
            return encodeMaterial(entry.getValue());
        }

        @Override
        public Iterator<Entry<BlockVector, MaterialData>> iterator() {
            return new EntryIterator();
        }
    }

    class BlockStateSet extends SetView<BlockState> {
        final World world;

        BlockStateSet(World world) {
            this.world = world;
        }

        @Override
        Class<?> entryArrayType() {
            return BlockState[].class;
        }

        @Override
        BlockState castEntry(Object obj) {
            return obj instanceof BlockState ? (BlockState) obj : null;
        }

        @Override
        long encodedKey(BlockState entry) {
            return encodePos(entry.getX(), entry.getY(), entry.getZ());
        }

        @Override
        int encodedValue(BlockState entry) {
            return encodeMaterial(entry.getTypeId(), entry.getRawData());
        }

        @Override
        public Iterator<BlockState> iterator() {
            return new BlockStateIterator();
        }

        class BlockStateIterator extends IteratorWrapper<BlockState, TLongIntIterator> {
            protected BlockStateIterator() {
                super(map.iterator());
            }

            @Override
            public BlockState next() {
                Block block = BlockUtils.blockAt(world, iter.key());
                if(block == null) return null;
                BlockState state = block.getState();
                state.setTypeId(decodeTypeId(iter.value()));
                state.setRawData(decodeMetadata(iter.value()));
                return state;
            }
        }
    }
}
