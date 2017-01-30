package tc.oc.pgm.filters.matcher.player;

import com.google.common.collect.Range;
import org.bukkit.attribute.Attribute;
import tc.oc.pgm.filters.matcher.TypedFilter;
import tc.oc.pgm.filters.query.IPlayerQuery;

public class AttributeFilter extends TypedFilter.Impl<IPlayerQuery> {

    private final Attribute attribute;
    private final Range<Double> range;

    public AttributeFilter(Attribute attribute, Range<Double> range) {
        this.attribute = attribute;
        this.range = range;
    }

    @Override
    public boolean matches(IPlayerQuery query) {
        return query.onlinePlayer()
                    .filter(player -> range.contains(player.getBukkit()
                                                           .getAttribute(Attribute.GENERIC_LUCK)
                                                           .getValue()))
                    .isPresent();
    }
}
