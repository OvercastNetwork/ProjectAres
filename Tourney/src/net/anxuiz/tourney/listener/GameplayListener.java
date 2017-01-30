package net.anxuiz.tourney.listener;

import javax.inject.Singleton;

import net.anxuiz.tourney.Tourney;
import net.anxuiz.tourney.TourneyState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tc.oc.pgm.events.MatchStateChangeEvent;

@Singleton
public class GameplayListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onStateChange(MatchStateChangeEvent event) {
        Tourney tourney = Tourney.get();
        switch(event.getNewState()) {
            case Finished:
                tourney.setState(TourneyState.ENABLED_FINISHED);
                break;

            case Running:
                tourney.setState(TourneyState.ENABLED_RUNNING);
                break;

            case Starting:
                tourney.setState(TourneyState.ENABLED_STARTING);
                break;

            case Idle:
                if(tourney.getMatchManager().getTeamManager().allTeamsMapped()) {
                    tourney.setState(TourneyState.ENABLED_WAITING_FOR_READY);
                } else {
                    tourney.setState(TourneyState.ENABLED_WAITING_FOR_TEAMS);
                }
                break;
        }
    }
}
