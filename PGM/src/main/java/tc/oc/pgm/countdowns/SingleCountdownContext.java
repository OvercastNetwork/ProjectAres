package tc.oc.pgm.countdowns;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import java.time.Duration;

public class SingleCountdownContext extends CountdownContext {

    private @Nullable Runner runner;

    @Override
    protected Stream<Runner> runners() {
        return runner != null ? Stream.of(runner)
                              : Stream.of();
    }

    @Override
    protected Optional<Runner> runner(Countdown countdown) {
        return runner != null && runner.countdown.equals(countdown) ? Optional.of(runner)
                                                                    : Optional.empty();
    }

    @Override
    protected void addRunner(Runner runner) {
        if(this.runner != null) {
            this.runner.cancel(false);
        }
        this.runner = runner;
    }

    @Override
    protected void removeRunner(Runner runner) {
        if(runner.equals(this.runner)) {
            this.runner = null;
        }
    }

    @Override
    public Set<Countdown> getAll() {
        return runner != null ? Collections.singleton(runner.countdown)
                              : Collections.emptySet();
    }

    public @Nullable Countdown getCountdown() {
        return runner != null ? runner.countdown
                              : null;
    }

    public @Nullable <T extends Countdown> T getCountdown(Class<T> type) {
        return runner != null && type.isInstance(runner.countdown) ? (T) runner.countdown
                                                                   : null;
    }

    protected Optional<Runner> runner() {
        return Optional.ofNullable(runner);
    }

    public Optional<Countdown> countdown() {
        return runner().map(Runner::countdown);
    }

    public <T extends Countdown> Optional<T> countdown(Class<T> type) {
        return runner().filter(r -> type.isInstance(r.countdown()))
                       .map(r -> (T) r.countdown());
    }

    public Optional<Duration> remaining() {
        return runner().map(Runner::remaining);
    }

    public Optional<Duration> remaining(Class<? extends Countdown> type) {
        return runner().filter(r -> type.isInstance(r.countdown()))
                       .map(Runner::remaining);
    }
}
