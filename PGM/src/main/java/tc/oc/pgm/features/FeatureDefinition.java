package tc.oc.pgm.features;

import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import tc.oc.commons.core.inspect.Inspectable;
import tc.oc.commons.core.reflect.Types;
import tc.oc.commons.core.util.Streams;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Parseable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Used as a base-class for all templates of features.  Kept around during parsing time
 * so that we can easily convert a definition into a Feature for match-time, and persist
 * the ID from the XML.  Also stored in the {@link FeatureDefinitionContext}
 * so that we don't collide IDs.
 *
 * @implNote FeatureDefinitions should NOT implement {@link #equals(Object)} or {@link #hashCode()}.
 * Any FD that is assigned an ID will be wrapped in a {@link FeatureReference}, which implements
 * equals/hashCode itself based on the ID alone. As such, FDs must always use object identity
 * for equality, so that the semantics are consistent with or without an ID.
 *
 * FDs are free to implement {@link Comparable}, but trying to compare an unresolved proxy
 * object will fail, so don't add FDs to any sorted collections before parsing is complete.
 */
public interface FeatureDefinition extends FeatureBase, Parseable {

    @Property
    default Optional<String> id() {
        return Optional.empty();
    }

    /**
     * Called at match load time
     */
    default void load(Match match) {}

    /**
     * Search the ancestry of the given type for a {@link FeatureInfo} annotation,
     * and return the type with the annotation. This is likely the most specific
     * type implemented by the feature, and the one that should be used in error
     * messages.
     */
    static @Nullable Class<? extends FeatureDefinition> findFeatureType(Class<? extends FeatureDefinition> type) {
        return Types.findAncestor(type, FeatureDefinition.class, t -> t.getAnnotation(FeatureInfo.class) != null);
    }

    /**
     * Return the most specific type implemented by this feature that can be used to
     * describe it to mapmakers i.e. in error mesages.
     */
    @Override
    default Class<? extends FeatureDefinition> getFeatureType() {
        return checkNotNull(findFeatureType(getClass()));
    }

    @Override
    default boolean isDefined() { return true; }

    @Override
    default void assertDefined() throws IllegalStateException {}

    @Override
    default FeatureDefinition getDefinition() { return this; }

    @Override
    default Optional<FeatureDefinition> tryDefinition() {
        return Optional.of(this);
    }

    /**
     * Concrete class of the definition object (rather than a proxy).
     *
     * Returns null if the feature is not defined yet.
     */
    @Override
    default @Nullable Class<? extends FeatureDefinition> getDefinitionType() {
        return getClass();
    }

    default boolean isInstanceOf(Class<? extends FeatureDefinition> type) {
        return type.isAssignableFrom(needDefinitionType());
    }

    default <T extends FeatureDefinition> T asInstanceOf(Class<T> type) {
        return type.cast(getDefinition());
    }

    /**
     * Get a readable name for the feature represented by the given type
     */
    static String getFeatureName(Class<? extends FeatureDefinition> type) {
        final Class<? extends FeatureDefinition> feature = findFeatureType(type);
        if(feature != null) {
            return feature.getAnnotation(FeatureInfo.class).name();
        } else {
            return type.getSimpleName();
        }
    }

    /**
     * Get a readable name for the type of this feature
     */
    @Override
    default String getFeatureName() {
        return getFeatureName(getClass());
    }

    default void validate(FeatureValidationContext context) throws InvalidXMLException {}

    default Stream<? extends FeatureDefinition> dependencies() {
        return Stream.of();
    }

    default <T extends FeatureDefinition> Stream<? extends T> dependencies(Class<T> type) {
        return Streams.instancesOf(dependencies(), type);
    }

    default <T extends FeatureDefinition> Stream<? extends T> deepDependencies(Class<T> type) {
        Stream<? extends T> stream = dependencies(type).flatMap(dep -> dep.deepDependencies(type));
        if(isInstanceOf(type)) {
            stream = Stream.concat(Stream.of((T) this), stream);
        }
        return stream;
    }

    /**
     * Unfortunately, we can't override equals in this interface to implement proxy-awareness,
     * so all subclasses that can possibly be proxied must extend {@link FeatureDefinition.Impl}.
     */
    abstract class Impl extends Inspectable.Impl implements FeatureDefinition {

        @Nullable FeatureBase identityDelegate;

        @Override
        public final int hashCode() {
            if(identityDelegate == null) {
                identityDelegate = this;
            }
            return identityDelegate != this ? identityDelegate.hashCode()
                                            : super.hashCode();
        }

        @Override
        public final boolean equals(Object that) {
            if(identityDelegate == null) {
                identityDelegate = this;
            }
            return identityDelegate != this ? identityDelegate.equals(that)
                                            : super.equals(that);
        }
    }
}
