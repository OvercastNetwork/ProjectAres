package tc.oc.pgm.events;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;
import tc.oc.commons.core.util.Streams;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.ParticipantState;
import tc.oc.pgm.tracker.damage.DamageInfo;

public class MatchPlayerDamageEvent extends MatchEvent implements MatchPlayerEvent {

    private final EntityDamageEvent cause;
    private final MatchPlayer victim;
    private final DamageInfo info;

    public MatchPlayerDamageEvent(EntityDamageEvent cause, MatchPlayer victim, DamageInfo info) {
        super(victim.getMatch());
        this.cause = cause;
        this.victim = victim;
        this.info = info;
    }

    @Override
    public Stream<UUID> users() {
        return Streams.compact1(victim().getUniqueId(),
                                attacker().map(ParticipantState::getUniqueId));
    }

    @Override
    public Stream<MatchPlayer> players() {
        return Streams.compact1(victim, onlineAttacker());
    }

    public EntityDamageEvent cause() {
        return cause;
    }

    public DamageInfo info() {
        return info;
    }

    public MatchPlayer victim() {
        return victim;
    }

    public Optional<ParticipantState> attacker() {
        return Optional.ofNullable(info.getAttacker());
    }

    public Optional<MatchPlayer> onlineAttacker() {
        return attacker().flatMap(state -> Optional.ofNullable(state.getMatchPlayer()));
    }

    public boolean isAttacker(MatchPlayer player) {
        final ParticipantState attacker = info.getAttacker();
        return attacker != null && attacker.isPlayer(player);
    }

    public boolean isAttacker(Player entity) {
        final ParticipantState attacker = info.getAttacker();
        return attacker != null && attacker.isEntity(entity);
    }

    private static final HandlerList handlers = new HandlerList();
    @Override public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}
