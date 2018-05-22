package tc.oc.commons.bukkit.nick;

import java.util.Objects;
import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tc.oc.api.bukkit.friends.OnlineFriends;
import tc.oc.api.bukkit.users.OnlinePlayers;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.UserId;
import tc.oc.commons.bukkit.util.PlayerStates;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Functionality common to real and nicked identities
 */
public class IdentityImpl implements Identity {

    protected final OnlinePlayers onlinePlayers;
    protected final OnlineFriends friendMap;
    protected final IdentityProvider identityProvider;
    protected final PlayerStates playerStates;
    protected final PlayerId playerId;

    private final @Nullable String nickname;

    IdentityImpl(OnlinePlayers onlinePlayers, OnlineFriends friendMap, PlayerStates playerStates, IdentityProvider identityProvider, PlayerId playerId, @Nullable String nickname) {
        this.onlinePlayers = onlinePlayers;
        this.friendMap = friendMap;
        this.playerStates = playerStates;
        this.identityProvider = identityProvider;

        this.playerId = checkNotNull(playerId);
        this.nickname = nickname == null || nickname.equalsIgnoreCase(playerId.username()) ? null : nickname;
    }

    @Override
    public PlayerId getPlayerId() {
        return playerId;
    }

    public @Nullable Player getPlayer() {
        return onlinePlayers.find(getPlayerId());
    }

    @Override
    public String getRealName() {
        return playerId.username();
    }

    @Override
    public @Nullable String getNickname() {
        return nickname;
    }

    @Override
    public String getPublicName() {
        return getNickname() != null ? getNickname() : getRealName();
    }

    @Override
    public String getName(CommandSender viewer) {
        return isRevealed(viewer) ? getRealName() : getNickname();
    }

    @Override
    public boolean isCurrent() {
        final Player player = getPlayer();
        return player != null && equals(identityProvider.currentIdentity(player));
    }

    @Override
    public boolean isConsole() {
        return false;
    }

    @Override
    public boolean isDead(CommandSender viewer) {
        if(!isOnline(viewer)) return false;
        Player player = onlinePlayers.find(playerId);
        return player != null && playerStates.isDead(player);
    }

    @Override
    public boolean isFriend(CommandSender viewer) {
        return friendMap.areFriends(viewer, playerId);
    }

    @Override
    public boolean belongsTo(CommandSender sender) {
        return sender.getName().equals(getPlayerId().username());
    }

    @Override
    public Familiarity familiarity(CommandSender viewer) {
        return belongsTo(viewer) ? Familiarity.SELF
                                 : isFriend(viewer) ? Familiarity.FRIEND
                                                    : Familiarity.PERSON;
    }

    @Override
    public boolean isRevealed(CommandSender viewer) {
        return getNickname() == null || identityProvider.reveal(viewer, getPlayerId());
    }

    @Override
    public boolean isDisguised(CommandSender viewer) {
        return !isRevealed(viewer);
    }

    @Override
    public boolean isOnline(CommandSender viewer) {
        return getPlayer(viewer) != null;
    }

    /**
     * Get the online {@link Player} for this identity, if they are online,
     * and the given viewer is allowed to know this. That is true if this
     * is the player's current identity, or if both this and their current
     * identity can be revealed to the viewer.
     */
    @Override
    public @Nullable Player getPlayer(CommandSender viewer) {
        final Player player = getPlayer();
        if(player == null) return null;
        final Identity current = identityProvider.currentIdentity(player);
        return current.equals(this) || (isRevealed(viewer) && current.isRevealed(viewer)) ? player : null;
    }

    @Override
    public boolean belongsTo(UserId userId, CommandSender viewer) {
        return playerId.equals(userId) && isRevealed(viewer);
    }

    @Override
    public boolean isSamePerson(Identity identity, CommandSender viewer) {
        if(isRevealed(viewer) && identity.isRevealed(viewer)) {
            return playerId.equals(identity.getPlayerId());
        }

        if(isDisguised(viewer) && identity.isDisguised(viewer)) {
            return getNickname().equals(identity.getNickname());
        }

        return false;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o)
            return true;
        if(!(o instanceof Identity))
            return false;
        Identity identity = (Identity) o;
        return Objects.equals(getPlayerId(), identity.getPlayerId()) &&
               Objects.equals(getNickname(), identity.getNickname());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPlayerId(), getNickname());
    }
}
