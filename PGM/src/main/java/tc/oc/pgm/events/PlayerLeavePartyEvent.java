package tc.oc.pgm.events;

import javax.annotation.Nullable;

import org.bukkit.event.HandlerList;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.Party;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Called BEFORE a {@link MatchPlayer} leaves a {@link Party}
 *
 * @deprecated just use {@link PlayerChangePartyEvent} for everything
 */
@Deprecated
public class PlayerLeavePartyEvent extends PlayerPartyChangeEventBase {

    public PlayerLeavePartyEvent(MatchPlayer player, Party oldParty, @Nullable Party newParty) {
        super(player, checkNotNull(oldParty), newParty);
    }

    private static HandlerList handlers = new HandlerList();
    @Override public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}
