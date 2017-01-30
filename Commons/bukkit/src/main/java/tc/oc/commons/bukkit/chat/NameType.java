package tc.oc.commons.bukkit.chat;

import org.bukkit.command.CommandSender;
import tc.oc.commons.bukkit.nick.Identity;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * These are the parameters that determine how a player's name is
 * rendered for a specific viewer, in a specific context.
 * This is used as a cache key, so if a name changes appearance
 * without any of these parameters changing, the cache must be
 * invalidated by calling {@link CachingNameRenderer#invalidateCache}.
 */
public class NameType {

    public final NameStyle style;
    public final boolean online;    // Player is online
    public final boolean reveal;    // Player's true identity is visible
    public final boolean self;      // Player is viewing their own name
    public final boolean friend;    // Player is a friend (of the viewer)
    public final boolean dead;      // Player is dead

    public NameType(NameStyle style, boolean online, boolean reveal, boolean self, boolean friend, boolean dead) {
        this.style = checkNotNull(style);
        this.online = online;
        this.reveal = reveal;
        this.self = self;
        this.friend = friend;
        this.dead = dead;
    }

    public NameType(NameStyle style, Identity identity, CommandSender viewer) {
        this(style, identity.isOnline(viewer), identity.isRevealed(viewer), identity.belongsTo(viewer), identity.isFriend(viewer), identity.isDead(viewer));
    }

    @Override
    public boolean equals(Object o) {
        if(this == o)
            return true;
        if(!(o instanceof NameType))
            return false;
        NameType other = (NameType) o;
        return Objects.equals(style, other.style) &&
               online == other.online &&
               reveal == other.reveal &&
               self == other.self &&
               friend == other.friend &&
               dead == other.dead;
    }

    @Override
    public int hashCode() {
        return Objects.hash(style, online, reveal, self, friend, dead);
    }
}
