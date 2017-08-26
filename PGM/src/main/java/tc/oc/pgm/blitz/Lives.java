package tc.oc.pgm.blitz;

import net.md_5.bungee.api.chat.BaseComponent;
import tc.oc.api.docs.PlayerId;
import tc.oc.pgm.match.Competitor;

import javax.annotation.Nullable;

public interface Lives {

    /**
     * Original amount of lives.
     */
    int original();

    /**
     * Current amount of lives (may be larger than {@link #original()},
     * due to players getting addition lives via kits).
     */
    int current();

    /**
     * Add more to the current lives and include the player that
     * caused this change if applicable.
     */
    void add(@Nullable PlayerId cause, int delta);

    /**
     * Get the delta number of life changes this player has caused.
     */
    int changesBy(PlayerId player);

    /**
     * Are the amount of lives reduced when this player dies?
     */
    boolean applicableTo(PlayerId player);

    /**
     * Is this player the sole owner of these lives?
     */
    boolean owner(PlayerId playerId);

    /**
     * Are there no lives left?
     */
    boolean empty();

    /**
     * Get the competitor relation of these lives.
     */
    Competitor competitor();

    /**
     * Message sent to players notifying them how many lives they have left.
     */
    BaseComponent remaining();

    /**
     * Sidebar status of how many respawns a competitor has left.
     */
    BaseComponent status();

    /**
     * Message sent when a player gains or loses lives.
     */
    BaseComponent change(int delta);

    /**
     * Implementations of lives as an enum.
     */
    Type type(); enum Type { TEAM, INDIVIDUAL }

}
