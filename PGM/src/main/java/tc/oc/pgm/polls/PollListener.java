package tc.oc.pgm.polls;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchManager;

public class PollListener implements Listener {
    @SuppressWarnings("unused")
    private final PollManager pollManager;
    private final MatchManager mm;

    public PollListener(PollManager pollManager, MatchManager mm) {
        this.pollManager = pollManager;
        this.mm = mm;
    }

    @EventHandler
    public void onPollEnd(PollEndEvent event) {
        if(event.getReason() == PollEndReason.Completed) {
            Match match = this.mm.getCurrentMatch();
            if(event.getPoll().isSuccessful()) {
                match.sendMessage(Poll.normalize + "The poll " + event.getPoll().getDescriptionMessage()
                        + Poll.normalize + " has succeeded" + Poll.seperator);
                match.sendMessage(event.getPoll().formatForAgainst());
                event.getPoll().executeAction();
            } else {
                match.sendMessage(Poll.normalize + "The poll " + event.getPoll().getDescriptionMessage()
                        + Poll.normalize + " has failed" + Poll.seperator);
                match.sendMessage(event.getPoll().formatForAgainst());
            }
        }
    }
}
