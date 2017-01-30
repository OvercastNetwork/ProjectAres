package tc.oc.pgm.filters.matcher.player;

import tc.oc.pgm.filters.matcher.TypedFilter;
import tc.oc.pgm.filters.query.IPartyQuery;
import tc.oc.pgm.filters.query.IPlayerQuery;
import tc.oc.pgm.flag.FlagDefinition;
import tc.oc.pgm.flag.state.State;

public class CarryingFlagFilter extends TypedFilter.Impl<IPartyQuery> {

    private final @Inspect(brief=true) FlagDefinition flag;

    public CarryingFlagFilter(FlagDefinition flag) {
        this.flag = flag;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public boolean matches(IPartyQuery query) {
        final State state = query.feature(flag).state();
        if(query instanceof IPlayerQuery) {
            return ((IPlayerQuery) query).onlinePlayer()
                                         .filter(state::isCarrying)
                                         .isPresent();
        } else {
            return state.isCarrying(query.getParty());
        }
    }
}
