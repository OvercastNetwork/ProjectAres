package tc.oc.pgm.match;

import javax.inject.Inject;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import tc.oc.commons.bukkit.chat.ConsoleAudience;
import tc.oc.commons.core.chat.AbstractMultiAudience;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.chat.MultiAudience;
import tc.oc.pgm.filters.Filter;

/**
 * Provides various aggregate {@link Audience}s within a match
 */
public class MatchAudiences {

    private final Match match;
    private final Audience participants;
    private final Audience observers;
    private final ImmutableSet<ConsoleAudience> console;

    @Inject MatchAudiences(Match match, ConsoleAudience console) {
        this.match = match;
        this.console = ImmutableSet.of(console);
        this.participants = new MultiAudience(Iterables.concat(this.console, match.getParticipatingPlayers()));
        this.observers = new MultiAudience(Iterables.concat(this.console, match.getObservingPlayers()));
    }

    public Audience all() {
        return match;
    }

    public Audience participants() {
        return participants;
    }

    public Audience observers() {
        return observers;
    }

    public Audience filter(Filter filter) {
        return new AbstractMultiAudience() {
            @Override
            protected Iterable<? extends Audience> getAudiences() {
                return Iterables.concat(console, Iterables.filter(match.getPlayers(), player -> !filter.denies(player)));
            }
        };
    }
}
