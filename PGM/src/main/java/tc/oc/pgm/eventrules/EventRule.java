package tc.oc.pgm.eventrules;

import javax.annotation.Nullable;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.util.Vector;
import tc.oc.pgm.features.FeatureDefinition;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.kits.Kit;
import tc.oc.pgm.regions.Region;

public interface EventRule extends FeatureDefinition {

    EventRuleScope scope();

    Region region();

    Filter filter();

    Kit kit();

    boolean lendKit();

    Vector velocity();

    @Nullable BaseComponent message();

    boolean earlyWarning();

    static EventRule newEventFilter(EventRuleScope scope, Region region, Filter filter, @Nullable BaseComponent message, boolean earlyWarning) {
        return new EventRuleImpl(scope, region, filter, null, false, null, message, earlyWarning);
    }

    static EventRule newKitRegion(EventRuleScope scope, Region region, Filter filter, Kit kit, boolean lendKit) {
        return new EventRuleImpl(scope, region, filter, kit, lendKit, null, null, false);
    }

    static EventRule newVelocityRegion(EventRuleScope scope, Region region, Filter filter, Vector velocity) {
        return new EventRuleImpl(scope, region, filter, null, false, velocity, null, false);
    }
}

class EventRuleImpl extends FeatureDefinition.Impl implements EventRule {
    private final @Inspect EventRuleScope scope;
    private final @Inspect Region region;
    private final @Inspect Filter filter;
    private final @Inspect Kit kit;
    private final @Inspect boolean lendKit;
    private final @Inspect Vector velocity;
    private final @Inspect @Nullable BaseComponent message;
    private final @Inspect boolean earlyWarning;

    EventRuleImpl(EventRuleScope scope,
                  Region region,
                  Filter filter,
                  Kit kit,
                  boolean lendKit,
                  Vector velocity,
                  @Nullable BaseComponent message,
                  boolean earlyWarning) {
        this.scope = scope;
        this.region = region;
        this.filter = filter;
        this.kit = kit;
        this.lendKit = lendKit;
        this.velocity = velocity;
        this.message = message;
        this.earlyWarning = earlyWarning;
    }

    @Override
    public EventRuleScope scope() {
        return scope;
    }

    @Override
    public Region region() {
        return region;
    }

    @Override
    public Filter filter() {
        return filter;
    }

    @Override
    public Kit kit() {
        return kit;
    }

    @Override
    public boolean lendKit() {
        return lendKit;
    }

    @Override
    public Vector velocity() {
        return velocity;
    }

    @Override
    @Nullable
    public BaseComponent message() {
        return message;
    }

    @Override
    public boolean earlyWarning() {
        return earlyWarning;
    }
}
