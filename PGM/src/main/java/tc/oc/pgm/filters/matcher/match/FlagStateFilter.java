package tc.oc.pgm.filters.matcher.match;

import java.util.Optional;

import tc.oc.pgm.filters.matcher.TypedFilter;
import tc.oc.pgm.filters.query.IMatchQuery;
import tc.oc.pgm.flag.FlagDefinition;
import tc.oc.pgm.flag.Post;
import tc.oc.pgm.flag.state.State;

public class FlagStateFilter extends TypedFilter.Impl<IMatchQuery> {

    private final @Inspect(brief=true) FlagDefinition flag;
    private final @Inspect(brief=true) Optional<Post> post;
    private final @Inspect Class<? extends State> state;

    public FlagStateFilter(FlagDefinition flag, Optional<Post> post, Class<? extends State> state) {
        this.flag = flag;
        this.post = post;
        this.state = state;
    }

    @Override
    public String inspectType() {
        return "FlagState";
    }

    @Override
    public String toString() {
        return inspect();
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public boolean matches(IMatchQuery query) {
        final State current = query.feature(flag).state();
        return state.isInstance(current) && post.map(current::isAtPost).orElse(true);
    }
}
