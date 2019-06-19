package tc.oc.pgm.match;

import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.chat.MultiAudience;
import tc.oc.minecraft.api.entity.Player;
import tc.oc.pgm.filters.Filter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides various aggregate {@link Audience}s within a match
 */
@Singleton
public class MatchAudiences {

    private final Match match;
    private final Audiences audiences;

    @Inject
    MatchAudiences(Match match, Audiences audiences) {
        this.match = match;
        this.audiences = audiences;
    }

    public MultiAudience all() {
        return () -> Stream.of(audiences.console(), audiences.playerFilter(player -> match.player(player).isPresent()));
    }

    public MultiAudience participants() {
        return () -> Stream.of(audiences.console(), audiences.playerFilter(player -> match.participant(player).isPresent()));
    }

    public MultiAudience observers() {
        return () -> Stream.of(audiences.console(), audiences.playerFilter(player -> match.getObservingPlayers().contains(match.player(player).orElse(null))));
    }

    public MultiAudience filter(Filter filter) {
        final Set<Player> allowed = match.players().filter(player -> !filter.denies(player)).map(MatchPlayer::getBukkit).collect(Collectors.toSet());
        return () -> Stream.of(audiences.console(), audiences.playerFilter(allowed::contains));
    }
}
