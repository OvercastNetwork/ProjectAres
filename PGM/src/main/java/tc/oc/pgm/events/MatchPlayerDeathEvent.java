package tc.oc.pgm.events;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import tc.oc.commons.core.util.Streams;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchPlayerState;
import tc.oc.pgm.match.ParticipantState;
import tc.oc.pgm.match.PlayerRelation;
import tc.oc.pgm.tracker.damage.DamageInfo;

import static com.google.common.base.Preconditions.checkNotNull;

public class MatchPlayerDeathEvent extends MatchEvent implements MatchPlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    protected final PlayerDeathEvent parent;
    protected final MatchPlayer victim;
    protected final DamageInfo damageInfo;
    protected final boolean predicted;

    protected int raindrops;

    public MatchPlayerDeathEvent(PlayerDeathEvent parent, MatchPlayer victim, DamageInfo damageInfo, boolean predicted) {
        super(checkNotNull(victim).getMatch());
        this.parent = checkNotNull(parent);
        this.victim = victim;
        this.damageInfo = checkNotNull(damageInfo);
        this.predicted = predicted;
    }

    @Override
    public Stream<UUID> users() {
        return Streams.compact1(getVictim().getUniqueId(),
                                killer().map(ParticipantState::getUniqueId));
    }

    @Override
    public Stream<MatchPlayer> players() {
        return Streams.compact1(getVictim(), onlineKiller());
    }

    public PlayerDeathEvent getParent() {
        return this.parent;
    }

    public MatchPlayer getVictim() {
        return victim;
    }

    public @Nullable ParticipantState getKiller() {
        return damageInfo.getAttacker();
    }

    public Optional<ParticipantState> killer() {
        return damageInfo.attacker();
    }

    public @Nullable MatchPlayer getOnlineKiller() {
        ParticipantState killer = getKiller();
        return killer == null ? null : killer.getMatchPlayer();
    }

    public Optional<MatchPlayer> onlineKiller() {
        return killer().flatMap(MatchPlayerState::onlinePlayer);
    }

    public EntityDamageEvent.DamageCause getCause() {
        return this.getVictim().getBukkit().getLastDamageCause().getCause();
    }

    public DamageInfo getDamageInfo() {
        return damageInfo;
    }

    public void setDeathMessage(String message) {
        this.parent.setDeathMessage(message);
    }

    public boolean isPredicted() {
        return this.predicted;
    }

    public boolean isVictim(MatchPlayer player) {
        return getVictim() == player;
    }

    public boolean isKiller(MatchPlayer player) {
        ParticipantState killer = getKiller();
        return killer != null && killer.isPlayer(player);
    }

    public boolean isInvolved(MatchPlayer player) {
        return isVictim(player) || isKiller(player);
    }

    /**
     * Get the relationship between the victim and killer
     */
    public PlayerRelation getRelation() {
        return PlayerRelation.get(getVictim().getParticipantState(), getKiller());
    }

    /**
     * Is the victim's death caused by a teammate?
     */
    public boolean isTeamKill() {
        return PlayerRelation.ALLY == getRelation();
    }

    /**
     * Is the victim's death caused by a player on another team?
     */
    public boolean isEnemyKill() {
        return PlayerRelation.ENEMY == getRelation();
    }

    /**
     * Did the victim kill themselves?
     */
    public boolean isSuicide() {
        return PlayerRelation.SELF == getRelation();
    }

    /**
     * Are the victim and killer the same player? (but not necessarily on the same team)
     */
    public boolean isSelfKill() {
        return getKiller() != null && getKiller().isPlayer(getVictim());
    }

    /**
     * Is this an enemy kill that was actually challenging? (i.e. not killing themselves)
     */
    public boolean isChallengeKill() {
        return isEnemyKill() && !isSelfKill();
    }

    public int getRaindrops() {
        return raindrops;
    }

    public void setRaindrops(int raindrops) {
        this.raindrops = raindrops;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}