package tc.oc.pgm.events;

import org.bukkit.event.HandlerList;

import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.MatchPlayer;

import java.util.Optional;

public class MatchScoreChangeEvent extends MatchEvent {
    private static final HandlerList handlers = new HandlerList();

    private final Competitor competitor;
    private final Optional<MatchPlayer> player;
    private final double oldScore;
    private final double newScore;

    public MatchScoreChangeEvent(Match match, Competitor competitor, Optional<MatchPlayer> player, double oldScore, double newScore) {
        super(match);
        this.competitor = competitor;
        this.player = player;
        this.oldScore = oldScore;
        this.newScore = newScore;
    }

    public Competitor getCompetitor() {
        return this.competitor;
    }

    public Optional<MatchPlayer> getPlayer() {
        return player;
    }

    public double getOldScore() {
        return this.oldScore;
    }

    public double getNewScore() {
        return this.newScore;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
