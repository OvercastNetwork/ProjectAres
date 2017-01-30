package tc.oc.pgm.countdowns;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class MultiCountdownContext extends CountdownContext {

    protected final Map<Countdown, Runner> runners = new HashMap<>();

    public Set<Countdown> getAll() {
        return ImmutableSet.copyOf(runners.keySet());
    }

    @Override
    protected Stream<Runner> runners() {
        return ImmutableList.copyOf(runners.values()).stream();
    }

    @Override
    protected Optional<Runner> runner(Countdown countdown) {
        return Optional.ofNullable(runners.get(countdown));
    }

    @Override
    protected void addRunner(Runner runner) {
        runners.put(runner.countdown, runner);
    }

    @Override
    protected void removeRunner(Runner runner) {
        runners.remove(runner.countdown, runner);
    }
}
