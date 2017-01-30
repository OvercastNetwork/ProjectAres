package tc.oc.commons.core.inject;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.ConstructorBinding;
import com.google.inject.spi.ConvertedConstantBinding;
import com.google.inject.spi.DefaultBindingTargetVisitor;
import com.google.inject.spi.DefaultElementVisitor;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;
import com.google.inject.spi.InjectionPoint;
import com.google.inject.spi.InjectionRequest;
import com.google.inject.spi.InstanceBinding;
import com.google.inject.spi.LinkedKeyBinding;
import com.google.inject.spi.PrivateElements;
import com.google.inject.spi.ProviderBinding;
import com.google.inject.spi.ProviderInstanceBinding;
import com.google.inject.spi.ProviderKeyBinding;
import com.google.inject.spi.ProviderLookup;
import com.google.inject.spi.StaticInjectionRequest;
import com.google.inject.spi.UntargettedBinding;

/**
 * Debugging tool that finds all dependencies for a set of modules, even implicit (JIT) ones.
 */
public class DependencyCollector {

    private static final Set<Class<?>> BLACKLIST = ImmutableSet.of(Injector.class, Logger.class);

    private final Set<Key<?>> requiredKeys = new HashSet<>();
    private final Set<Key<?>> implicitBindings = new HashSet<>();
    private final Map<Key<?>, Binding<?>> explicitBindings = new HashMap<>();

    private final SetMultimap<Key<?>, Dependency<?>> dependenciesByKey = HashMultimap.create();
    private final SetMultimap<InjectionPoint, Dependency<?>> dependenciesByInjectionPoint = HashMultimap.create();
    private final SetMultimap<TypeLiteral<?>, InjectionPoint> injectionPointsByType = HashMultimap.create();

    public Set<Key<?>> implicitBindings() { return implicitBindings; }
    public Map<Key<?>, Binding<?>> explicitBindings() {return explicitBindings; }
    public SetMultimap<Key<?>, Dependency<?>> dependenciesByKey() { return dependenciesByKey; }
    public SetMultimap<InjectionPoint, Dependency<?>> dependenciesByInjectionPoint() { return dependenciesByInjectionPoint; }
    public SetMultimap<TypeLiteral<?>, InjectionPoint> injectionPointsByType() { return injectionPointsByType; }

    public static void log(Logger logger, Level level, Iterable<? extends Module> modules) {
        new DependencyCollector().process(modules)
                                 .log(logger, level);
    }

    public DependencyCollector log(Logger logger, Level level) {
        logger.log(level, "Dumping all dependencies:");
        for(Map.Entry<TypeLiteral<?>, Collection<InjectionPoint>> entry : injectionPointsByType().asMap().entrySet()) {
            logger.log(level, entry.getKey().toString());
            for(InjectionPoint ip : entry.getValue()) {
                logger.log(level, "  " + ip.getMember());
                for(Dependency<?> dep : dependenciesByInjectionPoint().get(ip)) {
                    logger.log(level, "    " + dep);
                }
            }
        }
        return this;
    }

    public DependencyCollector clear() {
        requiredKeys.clear();
        implicitBindings.clear();
        explicitBindings.clear();
        return this;
    }

    public DependencyCollector processElements(Iterable<Element> elements) {
        final ElementVisitor visitor = new ElementVisitor();
        for(Element element : elements) {
            element.acceptVisitor(visitor);
        }
        processImplicitBindings();
        return this;
    }

    public DependencyCollector process(Module... modules) {
        processElements(Elements.getElements(modules));
        return this;
    }

    public DependencyCollector process(Iterable<? extends Module> modules) {
        processElements(Elements.getElements(Stage.TOOL, modules));
        return this;
    }

    private boolean requireKey(Key<?> key) {
        if(BLACKLIST.contains(key.getTypeLiteral().getRawType())) return false;
        return requiredKeys.add(Injection.dependencyKey(key));
    }

    private void processDependency(Dependency<?> dependency) {
        dependenciesByKey.put(dependency.getKey(), dependency);
        dependenciesByInjectionPoint.put(dependency.getInjectionPoint(), dependency);
        requireKey(dependency.getKey());
    }

    private void processInjectionPoint(InjectionPoint injectionPoint) {
        injectionPointsByType.put(injectionPoint.getDeclaringType(), injectionPoint);
        injectionPoint.getDependencies().forEach(this::processDependency);
    }

    private void processInjectionPoints(Iterable<InjectionPoint> injectionPoint) {
        injectionPoint.forEach(this::processInjectionPoint);
    }

    private void processInstanceInjections(TypeLiteral<?> type) {
        InjectionPoint.forInstanceMethodsAndFields(type).forEach(this::processInjectionPoint);
    }

    private void processInjections(TypeLiteral<?> type) {
        processInjectionPoint(InjectionPoint.forConstructorOf(type));
        processInstanceInjections(type);
    }

    private void processImplicitBindings() {
        for(;;) {
            ImmutableSet<Key<?>> keys = ImmutableSet.copyOf(Sets.difference(requiredKeys, Sets.union(explicitBindings.keySet(), implicitBindings)));
            if(keys.isEmpty()) break;
            for(Key<?> key : keys) {
                if(implicitBindings.add(key)) {
                    processInjections(key.getTypeLiteral());
                }
            }
        }
    }

    private class ElementVisitor extends DefaultElementVisitor<Object> {
        @Override
        public <T> Object visit(Binding<T> binding) {
            requireKey(binding.getKey());
            explicitBindings.put(binding.getKey(), binding);
            binding.acceptTargetVisitor(new BindingVisitor<>());
            return super.visit(binding);
        }

        @Override
        public <T> Object visit(ProviderLookup<T> providerLookup) {
            processDependency(providerLookup.getDependency());
            return super.visit(providerLookup);
        }

        @Override
        public Object visit(InjectionRequest<?> injectionRequest) {
            processInjectionPoints(injectionRequest.getInjectionPoints());
            return super.visit(injectionRequest);
        }

        @Override
        public Object visit(StaticInjectionRequest staticInjectionRequest) {
            processInjectionPoints(staticInjectionRequest.getInjectionPoints());
            return super.visit(staticInjectionRequest);
        }

        @Override
        public Object visit(PrivateElements privateElements) {
            processElements(privateElements.getElements());
            return super.visit(privateElements);
        }
    }

    private class BindingVisitor<T> extends DefaultBindingTargetVisitor<T, Object> {
        @Override
        public Object visit(InstanceBinding<? extends T> instanceBinding) {
            processInjectionPoints(instanceBinding.getInjectionPoints());
            return super.visit(instanceBinding);
        }

        @Override
        public Object visit(ProviderInstanceBinding<? extends T> providerInstanceBinding) {
            processInjectionPoints(providerInstanceBinding.getInjectionPoints());
            return super.visit(providerInstanceBinding);
        }

        @Override
        public Object visit(ProviderKeyBinding<? extends T> providerKeyBinding) {
            requireKey(providerKeyBinding.getProviderKey());
            return super.visit(providerKeyBinding);
        }

        @Override
        public Object visit(LinkedKeyBinding<? extends T> linkedKeyBinding) {
            requireKey(linkedKeyBinding.getLinkedKey());
            return super.visit(linkedKeyBinding);
        }

        @Override
        public Object visit(UntargettedBinding<? extends T> untargettedBinding) {
            processInjections(untargettedBinding.getKey().getTypeLiteral());
            return super.visit(untargettedBinding);
        }

        @Override
        public Object visit(ConstructorBinding<? extends T> constructorBinding) {
            processInjectionPoint(constructorBinding.getConstructor());
            processInjectionPoints(constructorBinding.getInjectableMembers());
            return super.visit(constructorBinding);
        }

        @Override
        public Object visit(ProviderBinding<? extends T> providerBinding) {
            requireKey(providerBinding.getProvidedKey());
            return super.visit(providerBinding);
        }

        @Override
        public Object visit(ConvertedConstantBinding<? extends T> convertedConstantBinding) {
            // TODO: What do I do here??
            return super.visit(convertedConstantBinding);
        }
    }
}
