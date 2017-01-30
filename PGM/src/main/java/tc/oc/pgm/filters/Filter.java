package tc.oc.pgm.filters;

import java.util.function.Predicate;
import java.util.stream.Stream;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import tc.oc.pgm.features.FeatureDefinition;
import tc.oc.pgm.features.FeatureInfo;
import tc.oc.pgm.filters.operator.AllFilter;
import tc.oc.pgm.filters.operator.AnyFilter;
import tc.oc.pgm.filters.operator.InverseFilter;
import tc.oc.pgm.filters.query.BlockQuery;
import tc.oc.pgm.filters.query.IQuery;

@FeatureInfo(name = "filter")
public interface Filter extends FeatureDefinition {
    /**
     * ALLOW or DENY the given {@link IQuery}, or ABSTAIN from responding.
     */
    QueryResponse query(IQuery query);

    /**
     * Return true if this filter ALLOWs the given {@link IQuery}, false if this filter DENYes it,
     * or throw a {@link UnsupportedOperationException} if this filter cannot respond to the given query.
     *
     * {@link #assertRespondsTo(Class)} can be used to ensure that this method will not throw.
     */
    default boolean response(IQuery query) {
        switch(query(query)) {
            case ALLOW: return true;
            case DENY:  return false;
            default:
                throw new UnsupportedOperationException("Filter did not respond to the query");
        }
    }

    /**
     * Return true only if ALL of the following conditions are true:
     *
     *   1. This filter always responds to queries of the given type (i.e. never ABSTAINS)
     *   2. This filter's response is derived only from properties of the given query type,
     *      and not on any properties in subtypes of that query.
     *   3. All dependencies of this filter also meet the above conditions.
     */
    boolean respondsTo(Class<? extends IQuery> queryType);

    /**
     * Throw a {@link FilterTypeException} unless this filter, and ALL dependency filters,
     * return true from {@link #respondsTo(Class)} for the given query type.
     *
     * If this filter has dependencies, it should call this method on them directly,
     * so that the exception contains the specific filter that failed the test.
     */
    default void assertRespondsTo(Class<? extends IQuery> queryType) throws FilterTypeException {
        if(!respondsTo(queryType)) {
            throw new FilterTypeException(this, queryType);
        }
    }

    /**
     * Does this filter support dynamic notifications?
     *
     * If this returns true, then any change in the response of this filter to
     * a query that passes {@link #assertRespondsTo(Class)} must notify {@link FilterListener}s
     * registered through {@link FilterMatchModule}.
     *
     * This method should NOT account for the behavior of any {@link #dependencies()},
     * as that is done automatically by the calling code. This method can return true
     * as long as it does NOT change its response to any query, without firing a notification,
     * at a time when none of its dynamic dependencies change their response.
     */
    default boolean isDynamic() { return false; }

    default Predicate<IQuery> respondsWith(QueryResponse response) {
        return q -> query(q) == response;
    }

    default Predicate<IQuery> isAllowed() {
        return respondsWith(QueryResponse.ALLOW);
    }

    default Predicate<IQuery> isDenied() {
        return respondsWith(QueryResponse.DENY);
    }

    /**
     * @see #and
     */
    default Filter not() {
        return new InverseFilter(this);
    }

    /**
     * Be careful not to call this on any undefined proxies while parsing.
     *
     * If one of the filters is static, you can call it on that one e.g.
     *
     *   MatchStateFilter.running().and(filter)
     *
     * instead of
     *
     *   filter.and(MatchStateFilter.running())
     */
    default Filter and(Filter that) {
        return this == that ? this : AllFilter.of(this, that);
    }

    /**
     * @see #and
     */
    default Filter or(Filter that) {
        return this == that ? this : AnyFilter.of(this, that);
    }

    enum QueryResponse {
        ALLOW,
        DENY,
        ABSTAIN;

        // TODO: Rename this to isNotDenied
        public boolean isAllowed() {
            return this == ALLOW || this == ABSTAIN;
        }

        public boolean isDenied() {
            return this == DENY;
        }

        public boolean isPresent() {
            return this != ABSTAIN;
        }

        public boolean toBoolean(boolean def) {
            switch(this) {
                case ALLOW: return true;
                case DENY: return false;
                default: return def;
            }
        }

        public QueryResponse orElse(QueryResponse def) {
            return isPresent() ? this : def;
        }

        public static QueryResponse any(QueryResponse... responses) {
            return any(Stream.of(responses));
        }

        public static QueryResponse any(Stream<QueryResponse> responses) {
            return responses.filter(QueryResponse::isPresent)
                            .reduce((a, b) -> QueryResponse.fromBoolean(a.isAllowed() || b.isAllowed()))
                            .orElse(ABSTAIN);
        }

        public static QueryResponse all(QueryResponse... responses) {
            return all(Stream.of(responses));
        }

        public static QueryResponse all(Stream<QueryResponse> responses) {
            return responses.filter(QueryResponse::isPresent)
                            .reduce((a, b) -> QueryResponse.fromBoolean(a.isAllowed() && b.isAllowed()))
                            .orElse(ABSTAIN);
        }

        public static QueryResponse first(QueryResponse... responses) {
            return first(Stream.of(responses));
        }

        public static QueryResponse first(Stream<QueryResponse> responses) {
            return responses.filter(QueryResponse::isPresent)
                            .findFirst()
                            .orElse(ABSTAIN);
        }

        public static QueryResponse fromBoolean(boolean allow) {
            return allow ? ALLOW : DENY;
        }
    }

    default QueryResponse query(Block block) {
        return query(new BlockQuery(block));
    }

    default QueryResponse query(BlockState block) {
        return query(new BlockQuery(block));
    }

    default boolean allows(IQuery query) {
        return query(query) == QueryResponse.ALLOW;
    }

    default boolean allows(Block block) {
        return query(block) == QueryResponse.ALLOW;
    }

    default boolean allows(BlockState block) {
        return query(block) == QueryResponse.ALLOW;
    }

    default boolean denies(IQuery query) {
        return query(query) == QueryResponse.DENY;
    }

    default boolean denies(Block block) {
        return query(block) == QueryResponse.DENY;
    }

    default boolean denies(BlockState block) {
        return query(block) == QueryResponse.DENY;
    }

    abstract class Impl extends FeatureDefinition.Impl implements Filter {}
}
