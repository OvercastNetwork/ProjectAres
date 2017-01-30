package tc.oc.pgm.filters.query;

import javax.annotation.Nullable;

import org.bukkit.event.Event;
import tc.oc.pgm.tracker.damage.DamageInfo;

import static com.google.common.base.Preconditions.checkNotNull;

public class DamageQuery extends PlayerEventQuery implements IDamageQuery {

    private final IPlayerQuery victim;
    private final DamageInfo damageInfo;

    protected DamageQuery(IPlayerQuery player, Event event, IPlayerQuery victim, DamageInfo damageInfo) {
        super(player, event);
        this.damageInfo = checkNotNull(damageInfo);
        this.victim = checkNotNull(victim);
    }

    public static DamageQuery victimDefault(@Nullable Event event, IPlayerQuery victim, DamageInfo damageInfo) {
        return new DamageQuery(victim, event, victim, damageInfo);
    }

    public static DamageQuery attackerDefault(@Nullable Event event, IPlayerQuery victim, DamageInfo damageInfo) {
        return new DamageQuery(damageInfo.getAttacker(), event, victim, damageInfo);
    }

    @Override
    public IPlayerQuery getVictim() {
        return victim;
    }

    @Override
    public DamageInfo getDamageInfo() {
        return damageInfo;
    }
}
