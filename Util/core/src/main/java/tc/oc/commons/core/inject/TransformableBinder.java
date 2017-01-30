package tc.oc.commons.core.inject;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Qualifier;

import com.google.inject.Binder;
import com.google.inject.BindingAnnotation;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.Multibinder;
import tc.oc.commons.core.reflect.ResolvableType;
import tc.oc.commons.core.reflect.TypeArgument;
import tc.oc.commons.core.reflect.Types;
import tc.oc.commons.core.util.Streams;

/**
 * Creates a transformable binding, which is associated with a set of {@link Transformer}s
 * that are applied every time the binding is provisioned. Each {@link Transformer} in the
 * chain can alter or replace the provisioned object.
 *
 * {@link Transformer}s are applied in the reverse order that they are bound.
 * The last one to be bound is the first to be called and the last to return.
 */
public class TransformableBinder<T> {

    private final Binder binder;
    private final Key<T> key;
    private final Key<T> untransformedKey;
    private final Key<Set<Transformer<T>>> transformerSetKey;
    private final Multibinder<Transformer<T>> transformerBinder;

    public TransformableBinder(Binder binder, @Nullable Class<T> type) {
        this(binder, type == null ? null : Key.get(type));
    }

    public TransformableBinder(Binder binder, @Nullable TypeLiteral<T> type) {
        this(binder, type == null ? null : Key.get(type));
    }

    public TransformableBinder(Binder binder, @Nullable Key<T> keyOrNull) {
        this.binder = binder.skipSources(TransformableBinder.class, TransformingProvider.class);
        this.key = keyOrNull != null ? keyOrNull : Key.get(new ResolvableType<T>(){}.in(getClass()));
        this.untransformedKey = Keys.get(key, new UntransformedImpl());

        final TypeLiteral<Transformer<T>> transformerType = new ResolvableType<Transformer<T>>(){}.with(new TypeArgument<T>(key.getTypeLiteral()){});
        final Annotation annotation = key.getAnnotation();
        this.transformerSetKey = Keys.get(Types.setOf(transformerType), annotation);
        this.transformerBinder = Multibinder.newSetBinder(this.binder, Keys.get(transformerType, annotation));

        this.binder.install(new TransformingProvider());
    }

    public LinkedBindingBuilder<T> bindOriginal() {
        return binder.bind(untransformedKey);
    }

    public LinkedBindingBuilder<Transformer<T>> bindTransformer() {
        return transformerBinder.addBinding();
    }

    @Qualifier @BindingAnnotation @Retention(RetentionPolicy.RUNTIME) private @interface Untransformed {
        boolean isWaterWet() default true; // Only here to trick Guice into thinking the annotation has attributes
    }

    private class UntransformedImpl implements Untransformed {
        Key<T> key() {
            return key;
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        @Override
        public boolean equals(Object that) {
            return that instanceof TransformableBinder.UntransformedImpl &&
                   this.key().equals(((UntransformedImpl) that).key());
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return Untransformed.class;
        }

        @Override
        public String toString() {
            return "@" + annotationType().getSimpleName() + "{" + key() + "}";
        }

        @Override
        public boolean isWaterWet() {
            return true;
        }
    }

    private class TransformingProvider extends KeyedManifest.Impl implements Provider<T> {

        Provider<T> original, transformed;
        Provider<Set<Transformer<T>>> transformers;

        protected TransformingProvider() {
            super(key);
        }

        @Override
        public void configure() {
            original = getProvider(untransformedKey);
            transformers = getProvider(transformerSetKey);
            bind(key).toProvider(this);
        }

        @Inject void buildTransformedProvider() {
            transformed = Streams.reduce(
                transformers.get().stream(),
                original,
                (provider, transformer) -> () -> transformer.transform(provider)
            );
        }

        @Override
        public T get() {
            return transformed.get();
        }
    }
}
