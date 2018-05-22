package tc.oc.commons.bukkit.nick;

import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.UserId;

/**
 * Captures aspects of a player's state that affect how they
 * are identified to other players. A player assumes a new
 * Identity whenever they change their nickname, or their
 * visibility (when we have /vanish). Two Identities are
 * equal if and only if they appear to be the same player
 * to all possible viewers.
 *
 * It should never be assumed that an {@link Identity} is the
 * player's *current* identity. The value of representing
 * identities with a concrete object is that they can be
 * stored and displayed even after the player who they belong
 * to has assumed a different identity.
 *
 * An {@link IdentityProvider} is used to create identities,
 * or to get the current identity of online players.
 */
public interface Identity {

    // Canonical properties

    /**
     * The ID of the player who used this identity
     */
    PlayerId getPlayerId();

    /**
     * The nickname used for this identity, or null if the player's real name is used
     */
    @Nullable String getNickname();


    // Derived properties

    /**
     * The (real) name of the player who used this identity
     */
    String getRealName();

    String getPublicName();

    @Nullable Player getPlayer();

    /**
     * Does this identity belong to the given sender?
     */
    boolean belongsTo(CommandSender sender);

    /**
     * Is the owner of this identity currently online and using this identity?
     */
    boolean isCurrent();

    /**
     * Does the identity belong to a console?
     */
    boolean isConsole();

    // Viewer-relative properties

    /**
     * The name of this identity as seen by the given viewer
     */
    String getName(CommandSender viewer);

    /**
     * The CURRENT online state of this identity as seen by the given viewer
     * (NOT the state at the time the identity was created)
     */
    boolean isOnline(CommandSender viewer);

    @Nullable Player getPlayer(CommandSender viewer);

    /**
     * The CURRENT living/dead state of this identity as seen by the given viewer
     * (NOT the state at the time the identity was created)
     */
    boolean isDead(CommandSender viewer);

    /**
     * Is this identity friends with the given viewer, and the viewer is allowed to know this?
     */
    boolean isFriend(CommandSender viewer);

    /**
     * How is this identity known to the given viewer?
     */
    Familiarity familiarity(CommandSender viewer);

    /**
     * Is this identity disguised for the given viewer?
     */
    boolean isDisguised(CommandSender viewer);

    /**
     * Should the true owner of this identity be revealed to the given viewer?
     */
    boolean isRevealed(CommandSender viewer);

    boolean belongsTo(UserId userId, CommandSender viewer);

    boolean isSamePerson(Identity identity, CommandSender viewer);
}
