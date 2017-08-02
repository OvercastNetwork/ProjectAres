package tc.oc.pgm.match;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import javax.inject.Inject;

import com.google.common.collect.ImmutableSet;
import org.bukkit.entity.Entity;
import org.bukkit.event.EntityAction;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.PlayerAction;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.player.PlayerEvent;
import tc.oc.commons.bukkit.event.UserEvent;
import tc.oc.commons.bukkit.event.targeted.TargetedEventRouter;
import tc.oc.commons.bukkit.event.targeted.TargetedEventRouterBinder;
import tc.oc.commons.bukkit.inject.BukkitFacetContext;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.stream.Collectors;
import tc.oc.commons.core.util.Optionals;
import tc.oc.commons.core.util.Streams;
import tc.oc.pgm.events.MatchPlayerEvent;
import tc.oc.pgm.events.MatchUserEvent;

public class MatchPlayerEventRouter implements TargetedEventRouter<Object> {

    public static class Manifest extends HybridManifest {

        private static final Set<Class<?>> EVENT_TYPES = ImmutableSet.of(
            EntityAction.class,
            EntityEvent.class,
            PlayerAction.class,
            PlayerEvent.class,
            UserEvent.class,
            MatchUserEvent.class,
            MatchPlayerEvent.class
        );

        @Override
        protected void configure() {
            bindAndExpose(MatchPlayerEventRouter.class);

            // Only register with the event types we can actually do something with.
            // We still want to get errors if we try to target an untargetable event.
            final TargetedEventRouterBinder routers = new TargetedEventRouterBinder(publicBinder());
            EVENT_TYPES.forEach(event -> routers.bindEvent(event).to(MatchPlayerEventRouter.class));
        }
    }

    @Inject MatchFinder finder;

    @Override
    public Stream<Listener> listeners(Object event) {
        return contexts(event).flatMap(BukkitFacetContext::listeners);
    }

    protected Stream<? extends BukkitFacetContext<?>> contexts(Object event) {
        // Try to get some online players from the event, either directly
        // through MatchPlayerEvent, or indirectly through entities.
        final Set<MatchPlayer> players;
        if(event instanceof MatchPlayerEvent) {
            players = ((MatchPlayerEvent) event).players().collect(Collectors.toImmutableSet());
        } else {
            final Set<Entity> entities = new HashSet<>();
            if(event instanceof EntityAction) entities.add(((EntityAction) event).getActor());
            if(event instanceof EntityEvent) entities.add(((EntityEvent) event).getEntity());
            if(event instanceof PlayerAction) entities.add(((PlayerAction) event).getActor());
            if(event instanceof PlayerEvent) entities.add(((PlayerEvent) event).getPlayer());

            players = entities.stream()
                              .flatMap(entity -> Streams.ofNullable(finder.getPlayer(entity)))
                              .collect(Collectors.toImmutableSet());
        }

        // If we have one or more MatchPlayers, return them along with their user contexts
        if(!players.isEmpty()) {
            return Stream.concat(
                players.stream(),
                players.stream().map(player -> player.userContext)
            );
        }

        // If we couldn't derive any online players from the event, try for offline player UUIDs
        final Set<UUID> uuids;
        if(event instanceof MatchUserEvent) {
            uuids = ((MatchUserEvent) event).users().collect(Collectors.toImmutableSet());
        } else if(event instanceof UserEvent) {
            uuids = ImmutableSet.of(((UserEvent) event).getUser().uuid());
        } else {
            return Stream.empty();
        }

        // Restrict to a specific match, if possible
        final Stream<Match> matches = finder.match((Event) event)
                                            .map(Stream::of)
                                            .orElseGet(() -> finder.currentMatches().stream());

        // Search the selected matches for both users and players
        // with the selected UUIDs.
        return matches.flatMap(
            match -> uuids.stream().flatMap(
                uuid -> Stream.concat(
                    Optionals.stream(match.player(uuid)),
                    Optionals.stream(match.userContext(uuid))
                )
            )
        );
    }
}
