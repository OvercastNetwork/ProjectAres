package net.anxuiz.tourney;

import java.util.Set;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.base.Preconditions;
import net.anxuiz.tourney.event.TourneyStateChangeEvent;
import net.anxuiz.tourney.listener.GameplayListener;
import net.anxuiz.tourney.listener.ReadyListener;
import net.anxuiz.tourney.listener.TeamListener;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.inject.MatchScoped;
import tc.oc.pgm.teams.Team;

@MatchScoped
public class MatchManager {

    private final Logger logger;
    private final Tourney tourney;
    private final Match match;
    private final Set<Team> teams;
    private final TeamManager teamManager;

    private final TeamListener teamListener;
    private final GameplayListener gameplayListener;
    private final ReadyListener readyListener;

    private @Nullable ReadyManager readyManager;

    private TourneyState state = TourneyState.DISABLED;
    private boolean recordQueued = true;

    @Inject MatchManager(Loggers loggers, Tourney tourney, Match match, Set<Team> teams, TeamManager teamManager, TeamListener teamListener, GameplayListener gameplayListener, ReadyListener readyListener, ReadyManager readyManager) {
        this.logger = loggers.get(getClass());
        this.tourney = tourney;
        this.match = match;
        this.teams = teams;
        this.teamManager = teamManager;
        this.teamListener = teamListener;
        this.gameplayListener = gameplayListener;
        this.readyListener = readyListener;
        this.readyManager = readyManager;
    }

    public Match getMatch() {
        return this.match;
    }

    public TeamManager getTeamManager() {
        return this.teamManager;
    }

    public @Nullable ReadyManager getReadyManager() {
        return this.readyManager;
    }

    /** Permanently clears the {@link ReadyManager} for the remainder of the match. */
    public void clearReadyManager() {
        this.readyManager = null;
    }

    public Set<Team> getTeams() {
        return teams;
    }

    public boolean isRecordQueued() {
        return recordQueued;
    }

    public void setRecordQueued(boolean recordQueued) {
        this.recordQueued = recordQueued;
    }

    public void recordMatchIfQueued() {
        if(recordQueued && tourney.getKDMSession() == null) {
            recordQueued = false;
            tourney.recordMatch(match.getId());
        }
    }

    public void setState(TourneyState newState) {
        if (this.state.equals(Preconditions.checkNotNull(newState, "State"))) return;
        TourneyState oldState = this.state;
        this.state = newState;

        logger.info("Transitioning to state " + newState);

        if(oldState == TourneyState.DISABLED) {
            match.registerEvents(teamListener);
            match.registerEvents(gameplayListener);
            match.registerEvents(readyListener);
        } else if(newState == TourneyState.DISABLED) {
            match.unregisterEvents(readyListener);
            match.unregisterEvents(gameplayListener);
            match.unregisterEvents(teamListener);
        }

        match.callEvent(new TourneyStateChangeEvent(match, oldState, newState));
    }

    public TourneyState getState() {
        return state;
    }
}
