package tc.oc.pgm.eventrules;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import org.bukkit.util.Vector;

public class EventRuleContext {
    protected final ArrayListMultimap<EventRuleScope, EventRule> byScope = ArrayListMultimap.create();
    protected final List<EventRule> byPriority = new ArrayList<>();

    public EventRuleContext() {
    }

    /**
     * Append the given rule, giving it the lowest priority
     */
    public void add(EventRule rule) {
        this.byScope.put(rule.scope(), rule);
        this.byPriority.add(rule);
    }

    /**
     * Prepend the given rule, giving it the highest priority
     */
    public void prepend(EventRule rule) {
        this.byScope.get(rule.scope()).add(0, rule);
        this.byPriority.add(0, rule);
    }

    /**
     * Return all rules in the given scope, in priority order
     */
    public Iterable<EventRule> get(EventRuleScope scope) {
        return this.byScope.get(scope);
    }

    /**
     * Return all rules in priority order
     */
    public Iterable<EventRule> getAll() {
        return this.byPriority;
    }

    public EventRule getNearest(Vector pos) {
        EventRule nearest = null;
        double distance = Double.POSITIVE_INFINITY;

        for(EventRule rule : byPriority) {
            double d = pos.distanceSquared(rule.region().getBounds().center());
            if(d < distance) {
                nearest = rule;
                distance = d;
            }
        }

        return nearest;
    }
}
