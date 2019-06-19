package tc.oc.pgm.payload.events;

import org.bukkit.event.HandlerList;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.payload.Payload;

import javax.annotation.Nullable;

public class CapturingTeamChangeEvent extends PayloadPointEvent {
    private static final HandlerList handlers = new HandlerList();
    @Nullable private final Competitor oldTeam;
    @Nullable private final Competitor newTeam;

    public CapturingTeamChangeEvent(Match match, Payload payload, Competitor oldTeam, Competitor newTeam) {
        super(match, payload);
        this.oldTeam = oldTeam;
        this.newTeam = newTeam;
    }

    public @Nullable
    Competitor getOldTeam() {
        return this.oldTeam;
    }

    public @Nullable
    Competitor getNewTeam() {
        return this.newTeam;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
