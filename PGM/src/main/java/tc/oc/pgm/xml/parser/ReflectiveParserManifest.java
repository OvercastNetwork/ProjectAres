package tc.oc.pgm.xml.parser;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.inject.ImplementedBy;
import com.google.inject.Key;
import com.google.inject.MembersInjector;
import com.google.inject.TypeLiteral;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.jdom2.Element;
import tc.oc.commons.core.formatting.StringUtils;
import tc.oc.commons.core.inject.Injection;
import tc.oc.commons.core.inject.InjectionChecks;
import tc.oc.commons.core.inject.KeyedManifest;
import tc.oc.commons.core.inspect.Inspectable;
import tc.oc.commons.core.inspect.InspectableProperty;
import tc.oc.commons.core.reflect.Members;
import tc.oc.commons.core.reflect.MethodFormException;
import tc.oc.commons.core.reflect.MethodHandleUtils;
import tc.oc.commons.core.reflect.Methods;
import tc.oc.commons.core.reflect.Types;
import tc.oc.commons.core.stream.Collectors;
import tc.oc.commons.core.util.CacheUtils;
import tc.oc.commons.core.util.ExceptionUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;
import tc.oc.pgm.xml.NodeSplitter;
import tc.oc.pgm.xml.Parseable;
import tc.oc.pgm.xml.UnrecognizedXMLException;
import tc.oc.pgm.xml.finder.NodeFinder;
import tc.oc.pgm.xml.validate.Validation;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Generates and binds a {@link ReflectiveParser} by reflecting on {@link T}.
 *
 * Does not bind {@link Parser}.
 */
public class ReflectiveParserManifest<T extends Parseable> extends KeyedManifest {

    private final TypeLiteral<T> type;
    private final Key<ReflectiveParser<T>> parserKey;
    private final Class<?> baseClass;

    public ReflectiveParserManifest(TypeLiteral<T> type) {
        this(type, Object.class);
    }

    public ReflectiveParserManifest(TypeLiteral<T> type, Class<?> baseClass) {
        this.type = type;
        this.parserKey = Key.get(Types.parameterizedTypeLiteral(ReflectiveParser.class, type));
        this.baseClass = checkNotNull(baseClass);

        checkArgument(type.getRawType().isInterface());
    }

    @Override
    protected Object manifestKey() {
        return type;
    }

    @Override
    protected void configure() {
        final ImplementedBy implementedBy = type.getRawType().getAnnotation(ImplementedBy.class);

        bind(parserKey).toInstance(new ReflectiveParserImpl(
            implementedBy != null ? implementedBy.value() : this.baseClass,
            Members.annotations(Parseable.Property.class, Methods.declaredMethodsInAncestors(type.getRawType()))
                   .merge(this::createProperty)
                   .collect(Collectors.toImmutableList())
        ));
    }

    private Property<T, ?> createProperty(Method method, Parseable.Property annotation) {
        if(method.getParameterTypes().length > 0) {
            throw new MethodFormException(method, "Property method cannot take parameters");
        }

        final TypeToken<?> outerType = Types.box(TypeToken.of(method.getGenericReturnType()));

        // If the property method is callable, and the outer type is not already Optional,
        // then it becomes intrinsically Optional. The proxy handles the logic of unwrapping
        // the value or calling the method.
        final Aggregator aggregator;
        if(Methods.isCallable(method) && !Types.isAssignable(Optional.class, outerType)) {
            aggregator = new OptionalAggregator<>(outerType);
        } else {
            aggregator = Aggregator.forType(outerType);
        }

        return createProperty(method, annotation, aggregator);
    }

    private <O, I> Property<O, I> createProperty(Method method, Parseable.Property annotation, Aggregator<O, I> aggregator) {
        // Generate names/aliases
        final String name = StringUtils.nonEmpty(annotation.name())
                                       .orElseGet(() -> method.getName().replace('_', '-'));
        final List<String> aliases = ImmutableList.copyOf(annotation.alias());

        // Custom NodeFinders
        final List<Class<? extends NodeFinder>> finders = Members
            .annotation(Parseable.Nodes.class, method)
            .map(annot -> Arrays.asList(annot.value()))
            .orElse(PropertyParser.DEFAULT_NODE_FINDERS);

        // Custom NodeSplitter
        final Class<? extends NodeSplitter> splitter = Members
            .annotation(Parseable.Split.class, method)
            .<Class<? extends NodeSplitter>>map(Parseable.Split::value)
            .orElse(NodeSplitter.Atom.class);

        // Validations
        final Parseable.Validate validate = method.getAnnotation(Parseable.Validate.class);
        final ImmutableList.Builder<Class<Validation<? super I>>> validations = ImmutableList.builder();
        if(validate != null) {
            for(Class<? extends Validation> validation : validate.value()) {
                if(!Validation.type(validation).isAssignableFrom(aggregator.innerTypeToken())) {
                    throw new MethodFormException(method, "Validation " + validation.getSimpleName() +
                                                          " is not applicable to property type " + aggregator.innerTypeToken());
                }
                validations.add((Class<Validation<? super I>>) validation);
            }
        }

        bindProxy(Types.parameterizedTypeLiteral(Parser.class, aggregator.innerTypeLiteral()));

        return new Property<>(
            method,
            new PropertyParser<>(
                binder(),
                name,
                aliases,
                finders,
                splitter,
                aggregator,
                validations.build()
            )
        );
    }

    private static class Property<O, I> {

        final Method method;
        final PropertyParser<O, I> parser;

        final InspectableProperty inspectableProperty = new InspectableProperty() {
            @Override public String name() {
                return parser.name();
            }

            @Override public Object value(Inspectable inspectable) throws Throwable {
                return method.invoke(inspectable);
            }
        };

        Property(Method method, PropertyParser<O, I> parser) {
            this.method = method;
            this.parser = parser;
        }
    }

    private class ReflectiveParserImpl implements ReflectiveParser<T> {

        final Class<? extends T> impl;
        final List<Property<?, ?>> properties;
        final Set<String> propertyNames;
        final MembersInjector<T> injector;

        ReflectiveParserImpl(Class<?> base, List<Property<?, ?>> properties) {
            InjectionChecks.checkInjectableCGLibProxyBase(base);

            this.properties = properties;
            this.propertyNames = properties.stream()
                                           .flatMap(property -> property.parser.names().stream())
                                           .collect(Collectors.toImmutableSet());

            final Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(base);
            enhancer.setInterfaces(new Class[]{ type.getRawType() });
            enhancer.setCallbackType(MethodInterceptor.class);
            enhancer.setUseFactory(true);
            this.impl = enhancer.createClass();
            this.injector = getMembersInjector((Class<T>) impl);
        }

        @Override
        public T parseElement(Element parent) throws InvalidXMLException {
            // Property methods mapped to their parsed values
            final ImmutableMap.Builder<Method, Object> builder = ImmutableMap.builder();

            // Remember if any property claimed the parent element
            final Set<Node> used = new HashSet<>();

            for(Property<?, ?> property : properties) {
                // Find all nodes for the property
                final Set<Node> nodes = property.parser.findNodes(parent)
                                                       .collect(Collectors.toImmutableSet());
                used.addAll(nodes);

                // Parse!
                final Object value = property.parser.parse(parent, nodes.stream());

                // Handle default value logic
                if(Methods.isCallable(property.method)) {

                    // If the method is callable, then the parsed type is always an Optional (see createProperty() above).
                    final Optional optional = (Optional) value;
                    if(optional.isPresent()) {

                        // If the parsed Optional is present, add it to the map.
                        // Unwrap it if necessary, to match the method's return type.
                        builder.put(property.method, Optional.class.isAssignableFrom(property.method.getReturnType()) ? optional : optional.get());
                    }

                    // If it's not present, leave it out of the map entirely, so that the proxy will call the method.
                } else {

                    // If the method is not callable, then the value will always match the method return type
                    builder.put(property.method, value);
                }
            }

            // Throw if any nodes were not parsed by any property
            final Node unused = findUnused(Node.of(parent), used);
            if(unused != null) {
                throw new UnrecognizedXMLException(unused);
            };

            // Create the proxy and inject it
            return ExceptionUtils.propagate(InvalidXMLException.class, Injection.unwrapExceptions(() -> {
                final T t = impl.newInstance();
                ((net.sf.cglib.proxy.Factory) t).setCallback(0, new Dispatcher(parent, builder.build()));
                injector.injectMembers(t);
                return t;
            }));
        }

        /**
         * Search the given node tree for the first completely unused node, if any.
         *
         * A node is unused if it's not in the used set, and neither are any of its ancestors or descendants.
         * Partly used nodes don't count as unused, but they are guaranteed to have a completely unused descendant.
         */
        private @Nullable Node findUnused(Node node, Set<Node> used) {
            // If node is used, return nothing
            if(used.contains(node)) return null;

            // Search for unused descendants
            final List<Node> children = node.nodes().collect(Collectors.toImmutableList());
            Node firstUnused = null;
            boolean partlyUsed = false;

            for(Node child : children) {
                final Node unused = findUnused(child, used);

                // Remember the first unused node we find
                if(unused != null && firstUnused == null) {
                    firstUnused = unused;
                }

                // The child itself is not unused, then some descendant must be used
                if(unused != child) {
                    partlyUsed = true;
                }
            }

            // If all children are completely unused, then this node is unused as well
            // Otherwise, return the first completely unused node that we found, if any
            return partlyUsed ? firstUnused : node;
        }

        private class Dispatcher implements MethodInterceptor {

            // These are the methods we want to intercept
            class Delegate implements Parseable {
                @Override
                public Optional<Element> sourceElement() {
                    return sourceNode;
                }

                @Override
                public Map<Method, Object> parsedValues() {
                    return values;
                }

                @Override
                public String inspectType() {
                    return type.getRawType().getSimpleName();
                }

                @Override
                public Stream<? extends InspectableProperty> inspectableProperties() {
                    return Stream.concat(Parseable.super.inspectableProperties(),
                                         properties.stream().map(property -> property.inspectableProperty));
                }
            }

            final Optional<Element> sourceNode;
            final ImmutableMap<Method, Object> values;
            final Delegate delegate;

            private Dispatcher(Element element, Map<Method, Object> values0) {
                this.values = ImmutableMap.copyOf(values0);
                this.sourceNode = Optional.of(element);
                this.delegate = new Delegate();
            }

            @Override
            public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
                // Give our delegate a chance to intercept, and cache the decision
                if(delegatedMethods.get(method, () -> method.getDeclaringClass() != Object.class &&
                                                      Methods.hasOverrideIn(Delegate.class, method))) {
                    return method.invoke(delegate, args);
                }

                // If we have a value for the property, return that
                final Object value = values.get(method);
                if(value != null) return value;

                // If there's no value, then the method MUST be callable (or the code is broken).
                // This can only fail for an abstract non-property method (which we should probably be checking for).
                if(method.isDefault()) {
                    // invokeSuper doesn't understand default methods
                    return defaultMethodHandles.get(method)
                                               .bindTo(obj)
                                               .invokeWithArguments(args);
                } else {
                    return proxy.invokeSuper(obj, args);
                }
            }
        }
    }

    private static final Cache<Method, Boolean> delegatedMethods = CacheUtils.newCache();
    private static final LoadingCache<Method, MethodHandle> defaultMethodHandles = CacheUtils.newCache(MethodHandleUtils::defaultMethodHandle);
}
