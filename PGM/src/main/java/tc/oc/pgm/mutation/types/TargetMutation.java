package tc.oc.pgm.mutation.types;

import java.time.Duration;
import java.util.List;

import tc.oc.commons.core.scheduler.Task;
import tc.oc.commons.core.stream.Collectors;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;

/**
 * A mutation module that executes a task on random {@link MatchPlayer}s.
 */
public abstract class TargetMutation extends MutationModule {

    private final Duration frequency;
    private Task task;

    public TargetMutation(final Match match, Duration frequency) {
        super(match);
        this.frequency = frequency;
    }

    /**
     * Execute a task on the given randomly selected players.
     * @param players a list of players, which size is determined by {@link #targets()}.
     */
    public abstract void execute(List<MatchPlayer> players);

    /**
     * Determine the number of random players to target.
     * If there are no enough players on the server, it is possible
     * that the number of targets is less than expected.
     * @return number of targets.
     */
    public abstract int targets();

    /**
     * Generate a list of random players.
     * @return the random players.
     */
    public List<MatchPlayer> search() {
        return match.participants()
                    .filter(MatchPlayer::isSpawned)
                    .collect(Collectors.toRandomSubList(entropy, targets()));
    }

    @Override
    public void enable() {
        super.enable();
        this.task = match.getScheduler(MatchScope.RUNNING).createRepeatingTask(frequency, () -> execute(search()));
    }

    @Override
    public void disable() {
        if(task != null) {
            task.cancel();
            task = null;
        }
        super.disable();
    }

}
