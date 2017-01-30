package tc.oc.pgm.polls;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;

public class PollManager {
    private final Plugin parent;
    private final BukkitScheduler scheduler;
    private final PluginManager pm;

    private Poll poll = null;
    private int pollTaskId = -1;

    public PollManager(Plugin parent) {
        this.parent = parent;
        this.scheduler = parent.getServer().getScheduler();
        this.pm = parent.getServer().getPluginManager();
    }

    /**
     * Gets the current poll if there is one running.
     * @return Current poll or null.
     */
    public Poll getPoll() {
        return this.poll;
    }

    /**
     * Indicates whether or not a poll is currently running.
     */
    public boolean isPollRunning() {
        return this.poll != null;
    }

    /**
     * Starts a new poll specified by the poll object.
     */
    public void startPoll(Poll poll) {
        if(!this.isPollRunning()) {
            this.pollTaskId = this.scheduler.scheduleSyncRepeatingTask(this.parent, poll, 0, 5*20);
            this.poll = poll;
            this.pm.callEvent(new PollStartEvent(poll));
        }
    }

    /**
     * Ends the poll with a specified reason.
     */
    public void endPoll(PollEndReason reason) {
        if(this.isPollRunning()) {
            this.pm.callEvent(new PollEndEvent(this.poll, reason));
            this.scheduler.cancelTask(this.pollTaskId);
            this.pollTaskId = -1;
            this.poll = null;
        }
    }
}
