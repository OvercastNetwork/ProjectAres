package tc.oc.pgm.mutation.submodule;

import com.google.common.collect.Range;
import java.time.Duration;
import tc.oc.commons.core.random.Entropy;
import tc.oc.commons.core.scheduler.Task;
import tc.oc.commons.core.stream.Collectors;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.ParticipantState;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A mutation module that executes a task on a random {@link ParticipantState}s.
 */
public abstract class TargetableMutationModule extends MutationModule {

    protected final Duration frequency;
    protected final int targets;
    protected Task task;

    public TargetableMutationModule(final Match match, Duration frequency, int targets) {
        super(match);
        this.frequency = checkNotNull(frequency, "frequency cannot be null");
        this.targets = targets; checkArgument(targets >= 1, "amount of targets cannot be less than 1");
        this.task = match.getScheduler(MatchScope.RUNNING).createRepeatingTask(frequency, () -> {
            final Entropy entropy = match.entropyForTick();
            match.participants()
                 .filter(MatchPlayer::isSpawned)
                 .collect(Collectors.toRandomSubList(entropy, entropy.randomInt(Range.closed(1, targets))))
                 .forEach(player -> execute(player.getParticipantState()));
        });
    }

    @Override
    public void disable(boolean premature) {
        task.cancel();
        super.disable(premature);
    }

    public abstract void execute(ParticipantState player);

}
