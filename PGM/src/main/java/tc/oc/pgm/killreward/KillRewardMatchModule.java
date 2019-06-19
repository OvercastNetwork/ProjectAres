package tc.oc.pgm.killreward;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.MatchPlayerDeathEvent;
import tc.oc.pgm.events.PlayerPartyChangeEvent;
import tc.oc.pgm.filters.query.DamageQuery;
import tc.oc.pgm.kits.ItemKitApplicator;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.ParticipantState;
import tc.oc.pgm.tracker.damage.DamageInfo;

@ListenerScope(MatchScope.RUNNING)
public class KillRewardMatchModule extends MatchModule implements Listener {

    private final List<KillReward> killRewards;
    private final Multimap<MatchPlayer, KillReward> deadPlayerRewards = ArrayListMultimap.create();

    public KillRewardMatchModule(Match match, List<KillReward> killRewards) {
        super(match);
        this.killRewards = killRewards;
    }

    public List<KillReward> rewards() {
        return killRewards;
    }

    public ImmutableList<KillReward> rewardsImmutable() {
        return ImmutableList.copyOf(killRewards);
    }

    public List<KillReward> rewards(@Nullable Event event, ParticipantState victim, DamageInfo damageInfo) {
        final DamageQuery query = DamageQuery.attackerDefault(event, victim, damageInfo);
        return rewardsImmutable().stream().filter(reward -> reward.filter.query(query).isAllowed()).collect(Collectors.toList());
    }

    public List<KillReward> rewards(MatchPlayerDeathEvent event) {
        return rewards(event, event.getVictim().getParticipantState(), event.getDamageInfo());
    }

    public void giveRewards(MatchPlayer killer, Collection<KillReward> rewards) {
        rewards.forEach(reward -> {
            // Apply kit first so it can not override reward items
            reward.kit.apply(killer);
            reward.items.forEach(stack -> ItemKitApplicator.fireEventAndTransfer(killer, stack));
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(MatchPlayerDeathEvent event) {
        if(!event.isChallengeKill()) return;
        MatchPlayer killer = event.getOnlineKiller();
        if(killer == null) return;

        List<KillReward> rewards = rewards(event);

        if(killer.isDead()) {
            // If a player earns a KW while dead, give it to them when they respawn. Rationale: If they click respawn
            // fast enough, they will get the reward anyway, and we can't prevent it in that case, so we might as well
            // just give it to them always. Also, if the KW is in itemkeep, they should definitely get it while dead,
            // and this is a relatively simple way to handle that case.
            deadPlayerRewards.putAll(killer, rewards);
        } else {
            giveRewards(killer, rewards);
        }
    }

    /**
     * This is called from {@link tc.oc.pgm.spawns.SpawnMatchModule} so that rewards are given after kits
     */
    public void giveDeadPlayerRewards(MatchPlayer attacker) {
        giveRewards(attacker, deadPlayerRewards.removeAll(attacker));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPartyChange(PlayerPartyChangeEvent event) {
        deadPlayerRewards.removeAll(event.getPlayer());
    }

}
