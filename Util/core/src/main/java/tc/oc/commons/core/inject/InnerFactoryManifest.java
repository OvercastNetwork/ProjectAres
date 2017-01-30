package tc.oc.commons.core.inject;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Set;
import javax.inject.Provider;

import com.google.inject.Key;
import com.google.inject.MembersInjector;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.HasDependencies;
import com.google.inject.spi.InjectionPoint;
import tc.oc.commons.core.reflect.ResolvableType;
import tc.oc.commons.core.reflect.TypeArgument;
import tc.oc.commons.core.reflect.Types;
import tc.oc.commons.core.stream.Collectors;

/**
 * Generate and install an {@link InnerFactory}
 * @param <O>    Outer type
 * @param <I>    Inner type
 */
public class InnerFactoryManifest<O, I> extends KeyedManifest {

    private final TypeLiteral<I> innerType;
    private final TypeLiteral<O> outerType;
    private final Key<InnerFactory<O, I>> factoryKey;

    public static <I> InnerFactoryManifest<?, I> forInnerClass(Class<I> type) {
        return forInnerClass(Key.get(type));
    }

    public static <I> InnerFactoryManifest<?, I> forInnerClass(TypeLiteral<I> type) {
        return forInnerClass(Key.get(type));
    }

    public static <I> InnerFactoryManifest<?, I> forInnerClass(Key<I> key) {
        final Class<?> outer = key.getTypeLiteral().getRawType().getEnclosingClass();
        if(outer == null) {
            throw new IllegalArgumentException(key + " is not an inner class");
        }
        return new InnerFactoryManifest(key, TypeLiteral.get(outer));
    }

    protected InnerFactoryManifest() {
        this(null, null);
    }

    public InnerFactoryManifest(Key<I> innerKey, TypeLiteral<O> outerType) {
        if(innerKey == null) {
            innerKey = Key.get(new ResolvableType<I>(){}.in(getClass()));
        }
        this.innerType = innerKey.getTypeLiteral();
        this.outerType = outerType != null ? outerType : new ResolvableType<O>(){}.in(getClass());

        this.factoryKey = innerKey.ofType(new ResolvableType<InnerFactory<O, I>>(){}
                                              .with(new TypeArgument<O>(this.outerType){},
                                                    new TypeArgument<I>(this.innerType){}));
    }

    @Override
    protected Object manifestKey() {
        return factoryKey;
    }

    @Override
    protected void configure() {
        final InjectionPoint point = InjectionPoint.forConstructorOf(innerType);
        final Constructor<I> constructor = (Constructor<I>) point.getMember();
        constructor.setAccessible(true);

        if(point.getDependencies().isEmpty() || !Types.isAssignable(point.getDependencies().get(0).getKey().getTypeLiteral(), outerType)) {
            addError("Expected %s to take %s as the first parameter of its injectable constructor", innerType, outerType);
            return;
        }

        final Set<Dependency<?>> dependencies = point.getDependencies()
                                                     .stream()
                                                     .skip(1)
                                                     .collect(Collectors.toImmutableSet());

        final List<Provider<?>> providers = dependencies.stream()
                                                        .map(dep -> getProvider(dep.getKey()))
                                                        .collect(Collectors.toImmutableList());

        final MembersInjector<I> membersInjector = getMembersInjector(innerType);

        class FactoryImpl implements InnerFactory<O, I>, HasDependencies {
            @Override
            public Set<Dependency<?>> getDependencies() {
                return dependencies;
            }

            public I create(O outer) {
                final Object[] args = new Object[providers.size() + 1];

                args[0] = outer;

                for(int i = 0; i < providers.size(); i++) {
                    args[i + 1] = providers.get(i).get();
                }

                return Injection.wrappingExceptions(() -> {
                    final I instance = constructor.newInstance(args);
                    membersInjector.injectMembers(instance);
                    return instance;
                });
            }
        }

        bind(factoryKey).toInstance(new FactoryImpl());
    }

}
