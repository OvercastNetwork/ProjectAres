package tc.oc.pgm.match;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import org.bukkit.entity.Player;
import tc.oc.api.bukkit.users.Users;
import tc.oc.api.docs.PlayerId;
import tc.oc.commons.core.chat.Audience;

public abstract class MultiPlayerParty implements Party {

    protected final Match match;
    private final Set<MatchPlayer> players = new HashSet<>();

    public MultiPlayerParty(Match match) {
        this.match = match;
    }

    /** Gets the match that this team is tied to.
     * @return Match this team is tied to.
     */
    @Override
    public Match getMatch() {
        return this.match;
    }

    @Override
    public boolean addPlayerInternal(MatchPlayer player) {
        return players.add(player);
    }

    @Override
    public boolean removePlayerInternal(MatchPlayer player) {
        return players.remove(player);
    }

    @Override
    public Set<MatchPlayer> getPlayers() {
        return players;
    }

    /**
     * Return the member of this team matching the given ID, or null if there
     * is no matching player currently on this team.
     */
    @Override
    public @Nullable MatchPlayer getPlayer(PlayerId playerId) {
        Player player = Users.player(playerId);
        if(player == null) return null;
        for(MatchPlayer teamPlayer : this.getPlayers()) {
            if(player == teamPlayer.getBukkit()) return teamPlayer;
        }
        return null;
    }

    @Override
    public Stream<? extends Audience> audiences() {
        return players();
    }
}
