package tc.oc.pgm.mutation.types;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import tc.oc.commons.core.stream.Collectors;
import tc.oc.commons.core.util.TimeUtils;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.Repeatable;
import tc.oc.time.Time;

/**
 * A mutation module that executes a task on random {@link MatchPlayer}s.
 */
public interface TargetMutation extends MutationModule {

    /**
     * Execute a task on the given randomly selected players.
     * @param players a list of players, which size is determined by {@link #targets()}.
     */
    void target(List<MatchPlayer> players);

    /**
     * Determine the number of random players to target.
     *
     * If there are no enough players on the server, it is possible
     * that the number of targets is less than expected.
     * @return number of targets.
     */
    int targets();

    /**
     * Get the next time {@link #target()} will be run.
     * @return next target time.
     */
    Instant next();

    /**
     * Set the next time {@link #target()} will be run.
     * @param time next target time.
     */
    void next(Instant time);

    /**
     * Get the frequency that {@link #target()} will be run.
     * @return frequency between target times.
     */
    Duration frequency();

    /**
     * Generate a list of random players.
     * @return the random players.
     */
    default List<MatchPlayer> search() {
        return match().participants()
                      .filter(MatchPlayer::isSpawned)
                      .collect(Collectors.toRandomSubList(entropy(), targets()));
    }

    /**
     * Execute a task on randomly selected players and reset the
     * next time the task will be executed.
     */
    default void target() {
        target(search());
        next(match().getInstantNow().plus(frequency()));
    }

    @Override
    default void enable() {
        MutationModule.super.enable();
        target();
    }

    @Repeatable(interval = @Time(seconds = 1))
    default void tick() {
        Instant now = match().getInstantNow(), next = next();
        if(next == null) {
            next(now.plus(frequency()));
        } else if(TimeUtils.isEqualOrBeforeNow(now, next)) {
            target();
        }
    }

    abstract class Impl extends MutationModule.Impl implements TargetMutation {

        Duration frequency;
        Instant next;

        public Impl(Match match, Duration frequency) {
            super(match);
            this.frequency = frequency;
        }

        @Override
        public Instant next() {
            return next;
        }

        @Override
        public void next(Instant time) {
            next = time;
        }

        @Override
        public Duration frequency() {
            return frequency;
        }

    }

}
