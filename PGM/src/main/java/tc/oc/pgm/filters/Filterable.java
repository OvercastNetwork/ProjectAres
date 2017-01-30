package tc.oc.pgm.filters;

import java.util.Optional;
import java.util.stream.Stream;

import tc.oc.pgm.filters.query.IMatchQuery;
import tc.oc.pgm.filters.query.IQuery;

/**
 * An object that {@link Filter}s can be applied to.
 *
 * Filterables serve as {@link IQuery}s about themselves in their current state,
 * and each Filterable subtype should also extend the query type given for its
 * {@link Q} parameter. Unlike {@link IQuery}s in general, which may be ephemeral
 * value types, Filterables have a persistent identity.
 *
 * Filterables are composed in a hiearchy that mirrors the type relationships
 * of their {@link Q} parameters. If query type B extends query type A then
 * every Filterable<B> is contained in some Filterable<A>. Aside from this,
 * the types of the Filterables may not have any particular relationship.
 */
public interface Filterable<Q extends IMatchQuery> extends IMatchQuery {

    /**
     * The (single) Filterable that contains this one, or empty if this
     * is a top-level object.
     */
    Optional<? extends Filterable<? super Q>> filterableParent();

    /**
     * Return the enclosing Filterable of the given subtype, if any.
     *
     * This object is returned if it extends the given type.
     */
    default <R extends Filterable<?>> Optional<R> filterableAncestor(Class<R> type) {
        return type.isInstance(this) ? Optional.of((R) this)
                                     : filterableParent().flatMap(parent -> parent.filterableAncestor(type));
    }

    /**
     * Return all {@link Filterable} objects that this object is directly composed of.
     *
     * This object is NOT included in the result, nor are indirect components i.e. grandchildren, etc.
     */
    Stream<? extends Filterable<? extends Q>> filterableChildren();

    /**
     * Return all individual objects of the given Filterable subtype that this object is composed of,
     * directly or indirectly, possibly including this object itself.
     *
     * This method simply tests this object's type, and recurses on all {@link #filterableChildren()}.
     * Subclasses should provide a more efficient implementation, if possible.
     */
    default <R extends Filterable<?>> Stream<? extends R> filterableDescendants(Class<R> type) {
        Stream<? extends R> result = filterableChildren().flatMap(child -> child.filterableDescendants(type));
        if(type.isInstance(this)) {
            result = Stream.concat(Stream.of((R) this), result);
        }
        return result;
    }
}
