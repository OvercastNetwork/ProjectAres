package tc.oc.pgm.match;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

import org.bukkit.EntityLocation;
import org.bukkit.entity.Player;
import tc.oc.api.docs.PlayerId;
import tc.oc.commons.bukkit.chat.NameStyle;
import tc.oc.commons.bukkit.chat.PlayerComponent;
import tc.oc.commons.bukkit.nick.Identity;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.chat.NullAudience;
import tc.oc.commons.core.util.Utils;
import tc.oc.pgm.filters.query.IPlayerQuery;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a "snapshot" view of a {@link MatchPlayer}.
 */
public class MatchPlayerState extends MatchEntityState implements IPlayerQuery {
    protected final Identity identity;
    protected final Party party;

    public MatchPlayerState(Match match, Identity identity, UUID uuid, Party party, EntityLocation location) {
        super(match, Player.class, uuid, location, null);
        checkNotNull(identity, "player");
        checkNotNull(party, "party");

        this.identity = identity;
        this.party = party;
    }

    public Identity getIdentity() {
        return identity;
    }

    @Override
    public PlayerComponent getStyledName(NameStyle style) {
        return new PlayerComponent(getIdentity(), style);
    }

    @Override
    public PlayerId getPlayerId() {
        return this.identity.getPlayerId();
    }

    @Override
    public Party getParty() {
        return this.party;
    }

    /**
     * Return the {@link MatchPlayer} referenced by this state, or null if the
     * player has switched parties or disconnected.
     */
    public @Nullable MatchPlayer getMatchPlayer() {
        return this.party.getPlayer(this.getPlayerId());
    }

    @Override
    public Optional<MatchPlayer> onlinePlayer() {
        return party.player(getPlayerId());
    }

    @Override
    public MatchPlayerState playerState() {
        return this;
    }

    @Override
    public Optional<ParticipantState> participantState() {
        return Optional.empty(); // Subclass overrides this
    }

    public boolean isPlayer(MatchPlayer player) {
        return this.getPlayerId().equals(player.getPlayerId());
    }

    public boolean isPlayer(MatchPlayerState player) {
        return this.getPlayerId().equals(player.getPlayerId());
    }

    public boolean canInteract() {
        return getParty().isParticipating();
    }

    public Audience getAudience() {
        MatchPlayer matchPlayer = getMatchPlayer();
        return matchPlayer == null ? NullAudience.INSTANCE : matchPlayer;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
               "{match=" + getMatch() +
               " party=" + getParty() +
               " player=" + getPlayerId() +
               "}";
    }

    @Override
    final public boolean equals(Object obj) {
        return Utils.equals(MatchPlayerState.class, this, obj, that ->
            this.getParty().equals(that.getParty()) &&
            this.getPlayerId().equals(that.getPlayerId())
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(getParty(), getPlayerId());
    }
}
