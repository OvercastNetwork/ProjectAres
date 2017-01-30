package tc.oc.commons.core.inject;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.ProviderWithDependencies;
import tc.oc.commons.core.stream.Collectors;

public class InjectingFactory<T> extends MemberInjectingFactory<T> implements ProviderWithDependencies<T> {

    private final List<Provider<?>> providers;

    @Inject public InjectingFactory(Injector injector, TypeLiteral<T> type) {
        super(type, injector.getMembersInjector(type));
        this.providers = injectionPoint.getDependencies()
                                       .stream()
                                       .map(dep -> injector.getProvider(dep.getKey()))
                                       .collect(Collectors.toImmutableList());
        dependencies.addAll(injectionPoint.getDependencies());
    }

    public InjectingFactory(Binder binder, TypeLiteral<T> type) {
        super(type, binder.getMembersInjector(type));
        this.providers = injectionPoint.getDependencies()
                                       .stream()
                                       .map(binder::getProvider)
                                       .collect(Collectors.toImmutableList());
        dependencies.addAll(injectionPoint.getDependencies());
    }

    @Override
    public T get() {
        final Object[] args = new Object[providers.size()];
        for(int i = 0; i < args.length; i++) {
            args[i] = providers.get(i).get();

        }
        return newInstance(args);
    }
}
