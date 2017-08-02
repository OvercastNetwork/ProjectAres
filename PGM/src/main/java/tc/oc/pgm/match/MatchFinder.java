package tc.oc.pgm.match;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

import org.bukkit.Physical;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import tc.oc.api.docs.UserId;
import tc.oc.commons.core.util.MapUtils;
import tc.oc.commons.core.util.Nullables;
import tc.oc.commons.core.util.Optionals;

public interface MatchFinder extends MatchPlayerFinder {

    Map<World, Match> matchesByWorld();

    @Override
    default @Nullable MatchPlayer getPlayer(@Nullable UUID uuid) {
        return Nullables.firstOrNull(currentMatches().stream().map(match -> match.getPlayer(uuid)));
    }

    @Override
    default @Nullable MatchPlayer getPlayer(@Nullable UserId userId) {
        return Nullables.firstOrNull(currentMatches().stream().map(match -> match.getPlayer(userId)));
    }

    @Override
    default @Nullable MatchPlayer getPlayer(@Nullable Player bukkit) {
        return Nullables.transform(getMatch(bukkit), match -> match.getPlayer(bukkit));
    }

    default Collection<Match> currentMatches() {
        return matchesByWorld().values();
    }

    /** Gets the match being played on a world, or null. */
    default @Nullable Match getMatch(World world) {
        return world == null ? null : matchesByWorld().get(world);
    }

    default @Nullable Match getMatch(Physical physical) {
        return physical == null ? null : getMatch(physical.getWorld());
    }

    default @Nullable Match getMatch(Event event) {
        return event instanceof Physical ? getMatch(((Physical) event))
                                         : null;
    }

    default Optional<Match> match(Physical physical) {
        return MapUtils.value(matchesByWorld(), physical.getWorld());
    }

    default Optional<Match> match(Event event) {
        return event instanceof Physical ? match((Physical) event)
                                         : Optional.empty();
    }

    default Match needMatch(World world) {
        final Match match = getMatch(world);
        if(match == null) {
            throw new IllegalStateException("No match available for world " + world.getName());
        }
        return match;
    }

    default Match needMatch(Physical physical) {
        return needMatch(physical.getWorld());
    }

    default Match needMatch(CommandSender sender) {
        return sender instanceof Physical ? needMatch((Physical) sender)
                                          : needCurrentMatch();
    }

    /** Gets the current match for a given command sender. */
    default @Nullable Match getCurrentMatch(CommandSender sender) {
        if(sender instanceof Physical) {
            return getMatch(((Physical) sender).getWorld());
        } else {
            return getCurrentMatch();
        }
    }

    /** Gets the current match that is running. */
    default @Nullable Match getCurrentMatch() {
        final Map<World, Match> matches = matchesByWorld();
        if(matches.isEmpty()) {
            return null;
        } else if(matches.size() == 1) {
            return matches.values().iterator().next();
        } else {
            throw new IllegalStateException("Called getCurrentMatch while multiple matches exist (probably during cycle)");
        }
    }

    default Match needCurrentMatch() {
        final Match match = getCurrentMatch();
        if(match == null) {
            throw new IllegalStateException("Tried to get the current match when no match was loaded");
        }
        return match;
    }

    default Optional<Match> currentMatch() {
        final Collection<Match> matches = matchesByWorld().values();
        return Optionals.getIf(matches.size() == 1, () -> matches.iterator().next());
    }
}
