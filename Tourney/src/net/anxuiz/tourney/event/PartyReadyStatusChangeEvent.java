package net.anxuiz.tourney.event;

import com.google.common.base.Preconditions;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import tc.oc.pgm.events.MatchEvent;
import tc.oc.pgm.match.Party;

/** Raised when a team readies. */
public class PartyReadyStatusChangeEvent extends MatchEvent {
    private final Party party;
    private final boolean formerStatus;
    private static final HandlerList handlers = new HandlerList();

    /**
     * Creates a new event.
     *
     * @param formerStatus The status before the change.
     * @param party         The team.
     */
    public PartyReadyStatusChangeEvent(boolean formerStatus, Party party) {
        super(party.getMatch());
        this.formerStatus = formerStatus;
        this.party = Preconditions.checkNotNull(party, "Team");
    }

    /**
     * Gets the former status.
     *
     * @return The former status.
     */
    public boolean getFormerStatus() {
        return this.formerStatus;
    }

    /**
     * Gets the new status.
     *
     * @return The new status.
     */
    public boolean getNewStatus() {
        return !this.formerStatus;
    }

    public static HandlerList getHandlerList() {
        return PartyReadyStatusChangeEvent.handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return PartyReadyStatusChangeEvent.handlers;
    }

    /**
     * Gets the {@link Party} in question.
     *
     * @return The team in question.
     */
    public Party getParty() {
        return this.party;
    }
}
