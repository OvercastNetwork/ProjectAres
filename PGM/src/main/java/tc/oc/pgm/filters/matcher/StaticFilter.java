package tc.oc.pgm.filters.matcher;

import java.util.Optional;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.query.IQuery;

public class StaticFilter extends Filter.Impl {
    protected final QueryResponse response;

    public StaticFilter(QueryResponse response) {
        this.response = response;
    }

    @Override
    public Optional<String> inspectIdentity() {
        return Optional.of(response.name());
    }

    @Override
    public boolean isDynamic() {
        return response.isPresent();
    }

    @Override
    public boolean respondsTo(Class<? extends IQuery> queryType) {
        return response.isPresent();
    }

    @Override
    public QueryResponse query(IQuery query) {
        return response;
    }

    @Override
    public QueryResponse query(Block block) {
        return response;
    }

    @Override
    public QueryResponse query(BlockState block) {
        return response;
    }

    public static final StaticFilter ALLOW = new StaticFilter(QueryResponse.ALLOW);
    public static final StaticFilter DENY = new StaticFilter(QueryResponse.DENY);
    public static final StaticFilter ABSTAIN = new StaticFilter(QueryResponse.ABSTAIN);
}
