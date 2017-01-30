package net.anxuiz.tourney.event.mapselect;

import com.google.common.base.Preconditions;
import net.anxuiz.tourney.vote.VetoVote;
import org.bukkit.event.Event;

public abstract class MapSelectionEvent extends Event {
    private final VetoVote vote;

    public MapSelectionEvent(final VetoVote vote) {
        this.vote = Preconditions.checkNotNull(vote, "Vote");
    }

    public VetoVote getVote() {
        return this.vote;
    }
}
