package tc.oc.pgm.filters.matcher.player;

import com.google.common.cache.LoadingCache;
import org.bukkit.PoseFlag;
import tc.oc.commons.core.util.CacheUtils;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.matcher.TypedFilter;
import tc.oc.pgm.filters.operator.AnyFilter;
import tc.oc.pgm.filters.operator.InverseFilter;
import tc.oc.pgm.filters.query.IPoseQuery;

public class PoseFilter extends TypedFilter.Impl<IPoseQuery> {

    private static final LoadingCache<PoseFlag, PoseFilter> CACHE = CacheUtils.newCache(PoseFilter::new);

    public static Filter of(PoseFlag pose) {
        return CACHE.getUnchecked(pose);
    }

    private static final Filter WALKING = new InverseFilter(AnyFilter.of(of(PoseFlag.SNEAKING), of(PoseFlag.SPRINTING)));
    public static Filter walking() { return WALKING; }

    private final @Inspect PoseFlag pose;

    public PoseFilter(PoseFlag pose) {
        this.pose = pose;
    }

    @Override
    public String toString() {
        return "Pose{" + pose + "}";
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public boolean matches(IPoseQuery query) {
        return query.getPose().contains(pose);
    }
}
