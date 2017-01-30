package tc.oc.pgm.cooldown;

import java.time.Duration;
import java.time.Instant;
import tc.oc.pgm.match.*;

import javax.inject.Inject;
import java.util.*;

import static tc.oc.commons.core.util.TimeUtils.isEqualOrBeforeNow;
import static tc.oc.commons.core.util.TimeUtils.plus;

public class CooldownPlayerFacet implements MatchPlayerFacet {

    private final Match match;
    private final WeakHashMap<Object, Instant> objects;

    @Inject CooldownPlayerFacet(Match match) {
        this.match = match;
        this.objects = new WeakHashMap<>();
    }

    @Repeatable(scope = MatchScope.LOADED)
    public void tick() {
        objects.entrySet().removeIf(entry -> isEqualOrBeforeNow(match.getInstantNow(), entry.getValue()));
    }

    /**
     * Submit a new cooldown for an object.
     */
    public void coolFor(Object object, Duration time) {
        if(objects.containsKey(object)) {
            throw new IllegalStateException(object + " already has an active cooldown");
        }
        if(!time.equals(Duration.ZERO)) {
            objects.put(object, plus(match.getInstantNow(), time));
        }
    }

    /**
     * Check if an object is cooling down.
     */
    public boolean isCooling(Object object) {
        return objects.containsKey(object);
    }

    /**
     * Check if an object is not cooling down.
     */
    public boolean isNotCooling(Object object) {
        return !isCooling(object);
    }

    /**
     * Find the expire time of an object's cooldown.
     */
    public Optional<Instant> expires(Object object) {
        return Optional.ofNullable(objects.get(object));
    }

}
