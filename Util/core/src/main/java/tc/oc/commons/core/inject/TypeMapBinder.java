package tc.oc.commons.core.inject;

import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

import com.google.common.reflect.TypeToken;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.MapBinder;
import tc.oc.commons.core.reflect.ResolvableType;
import tc.oc.commons.core.reflect.TypeArgument;
import tc.oc.commons.core.reflect.Types;
import tc.oc.commons.core.util.ImmutableTypeMap;
import tc.oc.commons.core.util.TypeMap;

/**
 * Binds the contents of an immutable {@link TypeMap} by wrapping a {@link MapBinder}.
 */
public class TypeMapBinder<K, V> {

    private final TypeLiteral<K> keyType;
    private final TypeLiteral<V> valueType;
    private final Key<TypeMap<K, V>> collectionKey;
    private final Key<Map<TypeToken<? extends K>, Set<V>>> backingCollectionKey;
    private final MapBinder<TypeToken<? extends K>, V> backingCollectionBinder;

    public TypeMapBinder(Binder binder, @Nullable TypeLiteral<K> keyType, @Nullable TypeLiteral<V> valueType) {
        this.keyType = keyType != null ? keyType : new ResolvableType<K>(){}.in(getClass());
        this.valueType = valueType != null ? valueType : new ResolvableType<V>(){}.in(getClass());

        final TypeArgument<K> keyTypeArg = new TypeArgument<K>(this.keyType){};
        final TypeArgument<V> valueTypeArg = new TypeArgument<V>(this.valueType){};

        this.collectionKey = Key.get(new ResolvableType<TypeMap<K, V>>(){}.with(keyTypeArg, valueTypeArg));
        this.backingCollectionKey = Key.get(new ResolvableType<Map<TypeToken<? extends K>, Set<V>>>(){}.with(keyTypeArg, valueTypeArg));

        this.backingCollectionBinder = MapBinder.newMapBinder(
            binder,
            new ResolvableType<TypeToken<? extends K>>(){}.with(keyTypeArg),
            this.valueType
        ).permitDuplicates();

        binder.install(new KeyedManifest.Impl(collectionKey) {
            @Override
            public void configure() {
                final Provider<Map<TypeToken<? extends K>, Set<V>>> backingCollectionProvider = getProvider(backingCollectionKey);
                bind(collectionType()).toProvider(() -> ImmutableTypeMap.copyOf(backingCollectionProvider.get()));
            }
        });
    }

    protected TypeMapBinder(Binder binder) {
        this(binder, null, null);
    }

    public static <K1, V1> TypeMapBinder<K1, V1> ofType(Binder binder, TypeLiteral<K1> keyType, TypeLiteral<V1> valueType) {
        return new TypeMapBinder<>(binder, Types.assertFullySpecified(keyType), Types.assertFullySpecified(valueType));
    }

    public static <K1, V1> TypeMapBinder<K1, V1> inContext(Binder binder, Class<?> declaringClass) {
        return new TypeMapBinder<>(binder, new ResolvableType<K1>(){}.in(declaringClass), new ResolvableType<V1>(){}.in(declaringClass));
    }

    public Key<TypeMap<K, V>> collectionKey() {
        return collectionKey;
    }

    public TypeLiteral<TypeMap<K, V>> collectionType() {
        return collectionKey().getTypeLiteral();
    }

    public LinkedBindingBuilder<V> addBinding(Class<? extends K> type) {
        return addBinding(TypeToken.of(type));
    }

    public LinkedBindingBuilder<V> addBinding(TypeLiteral<? extends K> type) {
        return addBinding(Types.toToken(type));
    }

    public LinkedBindingBuilder<V> addBinding(TypeToken<? extends K> type) {
        return backingCollectionBinder.addBinding(type);
    }
}
