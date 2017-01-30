package tc.oc.pgm.events;

import org.bukkit.event.HandlerList;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.Party;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Called AFTER a player leaves and/or joins a party. Subclasses are called for
 * more specific cases, and those should be used whenever possible.
 *
 * If the player is leaving the match, they will have already been removed when
 * this event is called, and their party will be set to null.
 *
 * @deprecated just use {@link PlayerChangePartyEvent} for everything
 */
@Deprecated
public class PlayerPartyChangeEvent extends PlayerPartyChangeEventBase {

    public PlayerPartyChangeEvent(MatchPlayer player, @Nullable Party oldParty, @Nullable Party newParty) {
        super(player, oldParty, newParty);
        checkArgument(oldParty != newParty);
    }

    private static final HandlerList handlers = new HandlerList();
    public static HandlerList getHandlerList() { return handlers; }
    @Override public HandlerList getHandlers() { return handlers; }
}
