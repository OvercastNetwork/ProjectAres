package tc.oc.pgm.features;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.google.common.reflect.TypeToken;
import gnu.trove.list.TIntList;
import org.jdom2.Element;
import tc.oc.commons.core.ListUtils;
import tc.oc.commons.core.collection.CountingStringMap;
import tc.oc.commons.core.inspect.Inspectable;
import tc.oc.commons.core.reflect.Methods;
import tc.oc.commons.core.reflect.Types;
import tc.oc.commons.core.util.CacheUtils;
import tc.oc.commons.core.util.CachingMethodHandleInvoker;
import tc.oc.commons.core.util.Comparables;
import tc.oc.commons.core.util.Optionals;
import tc.oc.commons.core.util.ProxyUtils;
import tc.oc.commons.core.util.ThrowingRunnable;
import tc.oc.commons.core.util.Utils;
import tc.oc.pgm.map.inject.MapScoped;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;
import tc.oc.pgm.xml.validate.Validatable;
import tc.oc.pgm.xml.validate.Validation;

import static com.google.common.base.Preconditions.*;
import static tc.oc.commons.core.exception.LambdaExceptionUtils.rethrowConsumer;
import static tc.oc.pgm.features.FeatureDefinition.getFeatureName;

/**
 * Store of {@link FeatureDefinition}s, supporting lookup by ID and type, and forward references through dynamic proxies.
 *
 * Every feature in a map is added to this context, with or without an ID. The right way to retrieve all features
 * of a particular type is through the {@link #all(Class)} method on this class.
 *
 * Features are added to the context through one of the {@link #define} methods. This can optionally include a
 * source {@link Element} which may be used for debugging and error messages.
 *
 * Features with an ID can be accessed through forward references using one of the {@link #reference} methods.
 * These methods can be called before the feature is defined. They will return a dynamic proxy of the specified
 * interface type that will delegate to the feature once it is defined.
 *
 * Validations can be registered through the {@link #validate} method, which accepts defined features or forward
 * reference proxies.
 *
 * After parsing, the {@link #postParse} method is called, which checks that all references resolve to a defined
 * feature that implements the interface specified by the reference. Then, all validations run.
 *
 * A feature with an ID is *always* accessed through a dynamic proxy. When an ID is given, the {@link #define}
 * method returns a proxy, just like {@link #reference}, and that proxy should replace the original object for
 * all purposes. This is necessary so that equality testing works in all cases. Referenced features must be
 * tested for equality using only their IDs, since that is the only thing known about them when a forward
 * reference is created. But {@link FeatureDefinition}s in general do not now their own ID, so even when the
 * feature is defined, it needs to be wrapped in a proxy that can lookup its ID in the context.
 *
 * This also means that equality testing on features *must* be done through {@link #equals}, and never with
 * the == operator. Proxies for the same feature might compare equal with == or they might not.
 *
 * Every method that returns a feature instance from the context requires the return interface type to be
 * specified explicitly. If the feature has an ID, then this type must be an interface, so that it can be
 * proxied. Due to forward references, the context may have to create proxies for features with only vague
 * knowledge of their final type e.g. GoalDefinition. This is why the type needs to be specified.
 *
 * Proxies always implement {@link FeatureProxy}, in addition to the requested type. This can be used to
 * detect them, and to get their ID.
 *
 * Trying to define a feature multiple times (i.e. passing the same object to {@link #define} multiple times)
 * will generate an exception. As such, it is important that parsers never try to reuse feature objects.
 * Every definition must be a unique object instance, even if they are otherwise identical.
 */
@MapScoped
public class FeatureDefinitionContext implements FeatureValidationContext, Comparator<FeatureDefinition> {

    // List of all Records of any type
    private final Set<Record<?>> records = new HashSet<>();

    // Defined records ordered lexically (i.e. order of their source Elements)
    private final NavigableSet<Record<?>> lexical = new TreeSet<>();

    // All records, indexed by definition. An IdentityHashMap is used to bypass
    // the equality logic used by proxies.
    private final Map<FeatureDefinition, Record> byDefinition = new IdentityHashMap<>();

    // IdentifiedRecords, indexed by ID
    private final Map<String, IdentifiedRecord> byId = new HashMap<>();

    // IdentifiedRecords and SluggedFeatureDefinition records, indexed by final slug,
    // which may be altered to make it unique.
    private final CountingStringMap<Record> bySlug = new CountingStringMap<>(1000, "--");

    private final List<Validatable> validatables = new ArrayList<>();

    @Inject private FeatureDefinitionContext() {}

    public enum Phase { PARSE, VALIDATE, FINISHED }

    private Phase phase = Phase.PARSE;

    public Phase phase() {
        return phase;
    }

    private void assertParsingComplete() {
        if(!Comparables.greaterThan(phase, Phase.PARSE)) {
            throw new IllegalStateException("Cannot perform this operation before parsing is complete");
        }
    }

    private @Nullable Record recordFor(FeatureDefinition feature) {
        return feature instanceof FeatureProxy ? byId.get(((FeatureProxy) feature).getId())
                                               : byDefinition.get(feature);
    }

    private Record needRecord(FeatureDefinition feature) {
        final Record record = recordFor(feature);
        if(record == null) {
            throw new IllegalStateException("No record of feature " + feature);
        }
        return record;
    }

    public @Nullable <T extends FeatureDefinition> T get(String id, Class<T> type) {
        return get(id, TypeToken.of(type));
    }

    /**
     * Return a feature with the given ID and type, or null if no such feature exists.
     * If the ID exists but is the wrong type, this method will still return null.
     */
    public @Nullable <T extends FeatureDefinition> T get(String id, TypeToken<T> type) {
        final IdentifiedRecord record = byId.get(id);
        if(record != null && record.assignableTo(type)) {
            return (T) record.direct();
        }
        return null;
    }

    public Stream<? extends FeatureDefinition> all() {
        return lexical.stream().map(Record::direct);
    }

    /**
     * Return all defined features of the given type, in the order they were defined.
     */
    public <T extends FeatureDefinition> Stream<T> all(Class<T> type) {
        return all(TypeToken.of(type));
    }

    public <T extends FeatureDefinition> Stream<T> all(TypeToken<T> type) {
        return lexical.stream()
                      .filter(record -> record.assignableTo(type))
                      .map(record -> (T) record.direct());
    }

    /**
     * Test if the context contains any features of the given type
     */
    public boolean containsAny(Class<? extends FeatureDefinition> type) {
        return lexical.stream()
                      .anyMatch(record -> record.assignableTo(TypeToken.of(type)));
    }

    /**
     * Get a guaranteed unique slug for the given feature.
     *
     * Must not be called until after parsing.
     */
    public String slug(SluggedFeatureDefinition feature) {
        assertParsingComplete();
        final String slug = needRecord(feature).slug();
        if(slug == null) {
            throw new IllegalStateException("Cannot generate a slug for " + feature.identify());
        }
        return slug;
    }

    /**
     * Return the XML source element that defines the given feature,
     * or null if the feature was defined without a source element.
     *
     * @throws IllegalStateException if given a proxy for an undefined feature
     */
    public @Nullable Element definitionNode(FeatureDefinition feature) {
        final Record record = recordFor(feature);
        return record == null ? null : record.definitionNode();
    }

    public @Nullable Node sourceNode(FeatureDefinition feature) {
        return feature instanceof FeatureReference ? ((FeatureReference) feature).referenceNode()
                                                   : Node.fromNullable(definitionNode(feature));
    }

    public String describeWithLocation(FeatureDefinition feature) {
        final Element node = definitionNode(feature);
        if(node == null) {
            return feature.getFeatureName();
        } else {
            return feature.getFeatureName() + " [" + new Node(node).describeWithLocation() + "]";
        }
    }

    @Override
    public int compare(FeatureDefinition a, FeatureDefinition b) {
        return needRecord(a).compareTo(needRecord(b));
    }

    @Override
    public <T extends Validatable> T validate(T validatable) throws InvalidXMLException {
        switch(phase) {
            case PARSE:
                validatables.add(validatable);
                break;

            case VALIDATE:
                validatable.validate();
                break;

            default:
                throw new IllegalStateException("Cannot validate in " + phase + " phase");
        }
        return validatable;
    }

    @Override
    public <T extends FeatureDefinition> T validate(T feature, @Nullable Node source, Stream<Validation<? super T>> validations) {
        final Node node = source != null ? source : sourceNode(feature);
        validations.forEach(rethrowConsumer(validation -> {
            final Validatable validatable = validation.bind(node).compose(() -> (T) feature.getDefinition());
            validate(validatable);
        }));
        return feature;
    }

    /**
     * Define the given feature with no source element or ID, and return it.
     *
     * Since the feature has no ID, a proxy is never returned.
     */
    public FeatureDefinition define(FeatureDefinition definition) throws InvalidXMLException {
        return define((Element) null, definition);
    }

    public <T extends FeatureDefinition> T define(Class<T> type, T definition) throws InvalidXMLException {
        return define(null, type, definition);
    }

    /**
     * Define the given feature with an optional ID, and no source node.
     *
     * This method does nothing if the given feature is a proxy, or is already defined.
     *
     * @return a proxy which must be used to access the feature
     */
    public FeatureDefinition define(@Nullable String id, FeatureDefinition definition) throws InvalidXMLException {
        return define(null, id, definition).direct();
    }

    /**
     * Define the given feature with an optional source element, and return it as a {@link FeatureDefinition}
     * (which may be a proxy, if the source element has an ID attribute).
     */
    public FeatureDefinition define(@Nullable Element source, FeatureDefinition definition) throws InvalidXMLException {
        return define(source, FeatureDefinition.class, definition);
    }

    /**
     * Define the given feature with an optional source element.
     *
     * The ID of the feature is parsed from the 'id' attribute of the given element, if present.
     *
     * An object of the given type is returned, which should be used to access the feature from now on.
     * If the feature has an ID, the type must be an interface, and the returned object will be a proxy.
     *
     * If the type is not an interface, and the source element has an ID element, then an
     * {@link InvalidXMLException} will be thrown.
     *
     * This method does nothing if the given feature is a proxy, or is already defined.
     */
    public <T extends FeatureDefinition> T define(@Nullable Element source, Class<T> type, T impl) throws InvalidXMLException {
        return define(source, source == null ? null : source.getAttributeValue("id"), type, impl);
    }

    public <T extends FeatureDefinition> T define(@Nullable Element source, @Nullable String id, Class<T> type, T impl) throws InvalidXMLException {
        if(id != null && !type.isInterface()) {
            // Can't proxy a non-interface type, so no references and no IDs
            throw new IllegalArgumentException("Cannot assign an ID to a " + impl.getFeatureName());
        }
        return define(source, id, impl).direct();
    }

    private <T extends FeatureDefinition> Record<T> define(@Nullable Element source, @Nullable String id, T definition) throws InvalidXMLException {
        if(definition instanceof FeatureProxy) {
            throw new IllegalArgumentException("Attempt to define proxy " + definition);
        }

        final Record<?> existing = byDefinition.get(definition);
        if(existing != null) {
            // Allow redefinition only with identical parameters
            if(Objects.equals(id, existing.id()) &&
               Objects.equals(source, existing.definitionNode())) return (Record<T>) existing;
            throw new IllegalArgumentException("Attempted redefinition of " + definition);
        }

        final Record<T> record;
        if(id == null) {
            // If feature has no ID, return a new anonymous record
            record = new AnonymousRecord<>(source, definition);
        } else {
            validateId(Node.fromNullable(source), id);

            // If there is an ID, get or create an identified record
            final IdentifiedRecord identified = byId.computeIfAbsent(id, IdentifiedRecord::new);
            record = (Record<T>) identified;

            // If the record is not defined yet, do that now, otherwise verify that
            // the new definition is the same as the existing one.
            if(!identified.isDefined()) {
                identified.define(source, definition);
            } else if(identified.definition() != definition) {
                throw new InvalidXMLException("The ID '" + id + "' is already in use by a " + identified.featureName(), source);
            }
        }

        // Ensure all of the feature's dependencies are also defined. If any have not
        // yet been defined, they will be defined now with no ID or source Element, and
        // any later attempt to redefine them with either of those will cause an error.
        definition.dependencies().forEach(rethrowConsumer(dep -> {
            if(!(dep instanceof FeatureProxy || byDefinition.containsKey(dep))) {
                define(dep);
            }
        }));

        // Let the feature register validations
        definition.validate(this);

        // And register the feature itself if it is a Validatable
        if(definition instanceof Validatable) {
            validate((Validatable) definition);
        }

        return record;
    }

    /**
     * Return a proxy for the feature with the given ID and type, which must be an interface.
     *
     * The returned object will implement the given interface, as well as {@link FeatureProxy}.
     * This can be used to distinguish proxies from real features.
     *
     * The proxy generally must not be accessed until after the post-parse phase,
     * when references are resolved. However, it is safe to call {@link #equals}
     * {@link #hashCode}, and {@link #toString}.
     *
     * @param source        The XML node that references the feature
     * @param type          The type required by the reference
     * @return              An object implementing the requested interface, that delegates to
     *                      the feature, once it is defined
     */
    public <T extends FeatureDefinition> T reference(Node source, String id, Class<T> type) throws InvalidXMLException {
        return byId.computeIfAbsent(checkNotNull(id), IdentifiedRecord::new)
                   .reference(type, checkNotNull(source));
    }

    public <T extends FeatureDefinition> T reference(Node source, Class<T> type) throws InvalidXMLException {
        return reference(source, source.getValueNormalize(), type);
    }

    private void detectDependencyCycles(FeatureDefinition feature, List<FeatureDefinition> stack) throws InvalidXMLException {
        final int i = stack.indexOf(feature);
        if(i >= 0) {
            throw new InvalidXMLException("Dependency cycle detected: " +
                                          Stream.concat(stack.subList(i, stack.size()).stream(), Stream.of(feature))
                                                .map(this::describeWithLocation)
                                                .collect(Collectors.joining(" -> ")),
                                          definitionNode(feature));
        }

        final List<FeatureDefinition> subStack = ListUtils.append(stack, feature);
        feature.dependencies(FeatureDefinition.class).forEach(rethrowConsumer(dep -> detectDependencyCycles(dep, subStack)));
    }

    /**
     * Resolve all references and run all validations
     */
    public Collection<InvalidXMLException> postParse() {
        phase = Phase.VALIDATE;
        final List<InvalidXMLException> errors = new ArrayList<>();

        try {
            records.forEach(r -> r.validateReferences(errors));
            if(!errors.isEmpty()) return errors;

            records.forEach(r -> r.validateDependencies(errors));
            if(!errors.isEmpty()) return errors;

            collectErrors(errors, null, () -> {
                for(Validatable validatable : validatables) {
                    validate(validatable);
                }
            });
            if(!errors.isEmpty()) return errors;

            records.forEach(Record::validateSlug);

            return errors;
        } finally {
            phase = Phase.FINISHED;
        }
    }

    private void collectErrors(Collection<InvalidXMLException> errors, @Nullable FeatureDefinition feature, ThrowingRunnable<InvalidXMLException> runnable) {
        try {
            runnable.runThrows();
        } catch(InvalidXMLException e) {
            if(e.getNode() == null && feature != null) {
                e.offerNode(sourceNode(feature));
            }
            errors.add(e);
        }
    }

    private void validateId(@Nullable Node source, String id) throws InvalidXMLException {
        if(id.length() == 0) {
            throw new InvalidXMLException("ID cannot be blank", source);
        }
    }

    private abstract class Record<F extends FeatureDefinition> implements Comparable<Record<?>> {

        @Nullable F definition;
        @Nullable String slug;
        @Nullable Element source;
        @Nullable TIntList path;

        public Record() {
            records.add(this);
        }

        abstract @Nullable String id();
        abstract F direct();
        abstract @Nullable String defaultSlug();

        F definition() {
            assertDefined();
            return definition;
        }

        boolean isDefined() {
            return definition != null;
        }

        void assertDefined() {
            if(!isDefined()) {
                final String id = id();
                throw new IllegalStateException("Cannot access undefined " + featureName() + (id == null ? "" : " with ID " + id));
            }
        }

        @Nullable String slug() {
            return slug;
        }

        void define(@Nullable Element source, F definition) throws InvalidXMLException {
            checkState(!isDefined());

            this.definition = checkNotNull(definition);
            if(source != null) this.source = source;

            // Find the lexical path of the source Element
            if(this.source != null) {
                this.path = XMLUtils.indexPath(this.source);
            }

            // Index by definition and source location
            byDefinition.put(definition, this);
            lexical.add(this);
        }

        /**
         * Records are sorted by lexical position of their definition Element.
         * Records without an Element are ordered before those with an Element,
         * and two different records NEVER compare equal.
         */
        @Override
        public int compareTo(Record<?> that) {
            assertDefined();
            if(this == that) return 0;
            if(this.path == null) {
                if(that.path == null) {
                    return Ordering.arbitrary().compare(this, that);
                } else {
                    return -1;
                }
            } else {
                if(that.path == null) {
                    return 1;
                } else {
                    return ListUtils.lexicalCompare(this.path, that.path);
                }
            }
        }

        @Nullable Element definitionNode() {
            assertDefined();
            return source;
        }

        boolean assignableTo(TypeToken<? extends FeatureDefinition> type) {
            return isDefined() && type.getRawType().isInstance(definition());
        }

        String featureName() {
            return definition().getFeatureName();
        }

        Class<? extends FeatureDefinition> featureType() {
            return definition().getFeatureType();
        }

        /**
         * Run validations that do not try to access any other records
         */
        void validateReferences(Collection<InvalidXMLException> errors) {}

        /**
         * Run the remaining validations that were not run in {@link #validateReferences(Collection)}
         *
         * {@link #validateReferences(Collection)} is called on ALL records before this method is called
         * on ANY of them, and if any errors are generated during the former phase, the latter is not run
         * at all.
         */
        void validateDependencies(Collection<InvalidXMLException> errors) {
            final F definition = definition();
            collectErrors(errors, definition, () -> detectDependencyCycles(definition, ImmutableList.of()));
        }

        void validateSlug() {
            // Resolve the slug only after parsing, in case it needs to access other features
            final String defaultSlug = defaultSlug();
            if(defaultSlug != null) {
                slug = bySlug.putReturningKey(defaultSlug, this);
            }
        }
    }

    /**
     * Record created for features defined without an ID. These features can never
     * be referenced, so they don't need proxies or anything fancy like that.
     */
    private class AnonymousRecord<F extends FeatureDefinition> extends Record<F> {

        private AnonymousRecord(@Nullable Element source, F definition) throws InvalidXMLException {
            define(source, definition);
        }

        @Override
        @Nullable String id() {
            return null;
        }

        @Override
        @Nullable String defaultSlug() {
            return definition() instanceof SluggedFeatureDefinition ? ((SluggedFeatureDefinition) definition()).defaultSlug() : null;
        }

        @Override
        F direct() {
            return definition;
        }
    }

    /**
     * Record created for features defined with an ID, or for forward references.
     */
    private class IdentifiedRecord extends Record<FeatureDefinition> {

        final String id;
        @Nullable FeatureProxy definitionProxy;

        final LoadingCache<ReferenceKey, Reference> referenceCache = CacheUtils.newCache(Reference::new);

        IdentifiedRecord(String id) {
            this.id = checkNotNull(id);
        }

        @Override
        String id() {
            return id;
        }

        @Override
        String defaultSlug() {
            return id();
        }

        @Override
        void define(@Nullable Element source, FeatureDefinition definition) throws InvalidXMLException {
            super.define(source, definition);

            // Set the identity delegate of the feature
            if(!(definition instanceof FeatureDefinition.Impl)) {
                throw new IllegalArgumentException("Cannot assign an ID to " + definition.getClass().getName() +
                                                   " because it does not extend " + FeatureDefinition.Impl.class.getName());
            }

            final FeatureDefinition.Impl impl = (FeatureDefinition.Impl) definition;
            if(impl.identityDelegate != null) {
                throw new IllegalStateException("Identity delegate is already set for " + definition +
                                                ", probably because something called equals() or hashCode() on it" +
                                                " before it was registered with the " + FeatureDefinitionContext.class.getSimpleName());
            }

            impl.identityDelegate = new Delegate();
        }

        Collection<Reference> references() {
            return referenceCache.asMap().values();
        }

        String featureName() {
            return isDefined() ? super.featureName()
                               : FeatureDefinition.getFeatureName(featureType());
        }

        Class<? extends FeatureDefinition> featureType() {
            return isDefined() ? super.featureType()
                               : Types.commonAncestor(FeatureDefinition.class, references().stream().map(ref -> ref.key.type)).get();
        }

        <T extends FeatureDefinition> T reference(Class<T> type, @Nullable Node source) {
            if(source == null && definition != null) {
                // If we have a definition and there is no source node, we can just
                // return the definition proxy instead of creating a new reference.
                checkArgument(type.isInstance(definition));
                return (T) direct();
            } else {
                return (T) referenceCache.getUnchecked(new ReferenceKey(type, source)).proxy;
            }
        }

        @Override
        FeatureDefinition direct() {
            assertDefined();
            if(definitionProxy == null) {
                definitionProxy = ProxyUtils.newProxy(FeatureProxy.class, Types.minimalInheritedInterfaces(definition.getClass()), new Delegate());
            }
            return (FeatureDefinition) definitionProxy;
        }

        @Override
        void validateReferences(Collection<InvalidXMLException> errors) {
            final Iterator<Reference> iter = references().iterator();
            if(iter.hasNext() && !isDefined()) {
                // Only generate one missing reference error per ID
                errors.add(new InvalidXMLException("Missing " + featureName() +
                                                   " with ID '" + id + "'",
                                                   iter.next().referenceNode()));
                return;
            }

            for(Reference reference : references()) {
                if(!reference.referenceType().isInstance(definition())) {
                    errors.add(new InvalidXMLException("Wrong type for ID '" + id +
                                                       "': expected a " + getFeatureName(reference.referenceType()) +
                                                       " rather than a " + featureName(),
                                                       reference.referenceNode()));
                }
            }

            super.validateReferences(errors);
        }

        /**
         * This object is mixed-in with all proxies, and handles any method calls
         * that it implements, which includes all the methods of {@link FeatureProxy}
         * and {@link Object}. Any calls that it does not implement are forwarded
         * to the feature definition.
         */
        class Delegate extends CachingMethodHandleInvoker implements FeatureProxy {

            @Override
            protected Object targetFor(Method method) {
                return Methods.respondsTo(this, method) ? this : definition();
            }

            @Override
            @Inspect
            public String getId() {
                return id;
            }

            @Override
            public FeatureDefinition getDefinition() {
                return definition();
            }

            @Override
            @Inspect(inline=true) // Append properties from definition
            public Optional<FeatureDefinition> tryDefinition() {
                return Optionals.getIf(isDefined(), IdentifiedRecord.this::definition);
            }

            @Override
            public String getFeatureName() {
                return featureName();
            }

            @Override
            public Class<? extends FeatureDefinition> getFeatureType() {
                return featureType();
            }

            @Override
            public Class<? extends FeatureDefinition> getDefinitionType() {
                return isDefined() ? definition().getClass() : null;
            }

            @Override
            public boolean isDefined() {
                return IdentifiedRecord.this.isDefined();
            }

            @Override
            public void assertDefined() throws IllegalStateException {
                IdentifiedRecord.this.assertDefined();
            }

            @Override
            public String inspectType() {
                return isDefined() ? definition().inspectType() : featureType().getSimpleName();
            }

            @Override
            public Optional<String> inspectIdentity() {
                return Optional.of(tryDefinition().flatMap(Inspectable::inspectIdentity)
                                                  .map(def -> def + ":" + id)
                                                  .orElse(id));
            }

            @Override
            public String toString() {
                return inspect();
            }

            @Override
            public FeatureDefinitionContext context() {
                return FeatureDefinitionContext.this;
            }

            @Override
            public boolean equals(Object that) {
                if(that instanceof FeatureProxy) {
                    // Proxies are equal if they have the same parent context and ID
                    final FeatureProxy thatDelegate = (FeatureProxy) that;
                    return this.context().equals(thatDelegate.context()) &&
                           this.getId().equals(thatDelegate.getId());
                } else {
                    // If the other object is not a proxy, the only other way it can
                    // be equal is if it is the definition for this record. In that
                    // case, it will have its identityDelegate set to some Delegate
                    // (possibly this one) that will compare equal to this Delegate.
                    return that != null && that.equals(this);
                }
            }

            @Override
            public int hashCode() {
                return Objects.hash(context(), getId());
            }
        }

        class Reference extends Delegate implements FeatureReference {
            final ReferenceKey key;
            final FeatureReference proxy;

            Reference(ReferenceKey key) {
                this.key = checkNotNull(key);
                this.proxy = ProxyUtils.newProxy(FeatureReference.class, ImmutableSet.of(key.type), this);
            }

            @Override
            @Inspect(name="reference")
            public Node referenceNode() {
                return key.source;
            }

            @Override
            public Class<? extends FeatureDefinition> referenceType() {
                return key.type;
            }
        }
    }

    private static class ReferenceKey {
        final Class<? extends FeatureDefinition> type;
        final @Nullable Node source;

        private ReferenceKey(Class<? extends FeatureDefinition> type, @Nullable Node source) {
            this.type = checkNotNull(type);
            this.source = source;
            checkArgument(type.isInterface());
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, source);
        }

        @Override
        public boolean equals(Object obj) {
            return Utils.equals(ReferenceKey.class, this, obj, that ->
                this.type.equals(that.type) &&
                Objects.equals(this.source, that.source)
            );
        }
    }
}
