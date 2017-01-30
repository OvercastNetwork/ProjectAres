package tc.oc.pgm.events;

import javax.annotation.Nullable;

import org.bukkit.event.HandlerList;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.Party;

/**
 * Fired *around* all party changes.
 *
 * The change happens when you {@link #yield()}.
 */
public class PlayerChangePartyEvent extends PlayerPartyChangeEventBase {

    public PlayerChangePartyEvent(MatchPlayer player, @Nullable Party oldParty, @Nullable Party newParty) {
        super(player, oldParty, newParty);
    }

    private static final HandlerList handlers = new HandlerList();
}
