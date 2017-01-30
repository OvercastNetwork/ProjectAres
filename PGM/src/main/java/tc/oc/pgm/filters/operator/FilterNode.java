package tc.oc.pgm.filters.operator;

import java.util.List;

import com.google.common.collect.ImmutableList;
import tc.oc.pgm.filters.Filter;

@Deprecated
public class FilterNode extends ChainFilter {
    public FilterNode(List<? extends Filter> parents, List<? extends Filter> allowedMatchers, List<? extends Filter> deniedMatchers) {
        super(ImmutableList.of(FallthroughFilter.deny(deniedMatchers),
                               FallthroughFilter.allow(allowedMatchers),
                               ChainFilter.reverse(parents)));
    }
}
