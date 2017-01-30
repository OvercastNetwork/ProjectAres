package tc.oc.pgm.tracker.trackers;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Inject;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.MatchPlayerDeathEvent;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.tracker.EventResolver;
import tc.oc.pgm.tracker.damage.DamageInfo;
import tc.oc.pgm.tracker.damage.GenericDamageInfo;

/**
 * - Resolves all damage done to players and tracks the most recent one
 * - Wraps {@link PlayerDeathEvent}s in a {@link MatchPlayerDeathEvent}, together with the causing info
 * - Displays death messages
 */
@ListenerScope(MatchScope.RUNNING)
public class DeathTracker implements Listener {

    private final Logger logger;
    private final Match match;
    private final EventResolver eventResolver;

    private final Map<MatchPlayer, DamageInfo> lastDamageInfos = new HashMap<>();

    @Inject DeathTracker(Loggers loggers, Match match, EventResolver eventResolver) {
        this.logger = loggers.get(getClass());
        this.match = match;
        this.eventResolver = eventResolver;
    }

    // Trackers will do their cleanup at MONITOR level, so we listen at
    // HIGHEST to make sure all the info is still available.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        MatchPlayer victim = match.getParticipant(event.getEntity());
        if(victim == null) return;

        lastDamageInfos.put(victim, eventResolver.resolveDamage(event));
    }

    @Nullable DamageInfo getLastDamage(MatchPlayer victim) {
        DamageInfo info = lastDamageInfos.get(victim);
        if(info != null) return info;

        EntityDamageEvent damageEvent = victim.getBukkit().getLastDamageCause();
        if(damageEvent != null) {
            return eventResolver.resolveDamage(damageEvent);
        }

        return null;
    }

    /**
     * Must run after {@link tc.oc.pgm.spawns.SpawnMatchModule#onVanillaDeath}
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        logger.fine("Wrapping " + event);
        MatchPlayer victim = match.getParticipant(event.getEntity());
        if(victim == null || victim.isDead()) return;

        DamageInfo info = getLastDamage(victim);
        if(info == null) info = new GenericDamageInfo(EntityDamageEvent.DamageCause.CUSTOM);

        match.callEvent(new MatchPlayerDeathEvent(event, victim, info, CombatLogTracker.isCombatLog(event)));
    }
}
