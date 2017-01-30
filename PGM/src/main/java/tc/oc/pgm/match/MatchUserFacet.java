package tc.oc.pgm.match;

import tc.oc.commons.core.inject.Facet;

/**
 * A facet that shares a lifecycle with a single user in a single match.
 *
 * The first time a player joins a match, their user facets are loaded,
 * and they stay loaded until the match unloads. If the user leaves and
 * rejoins the match, they will get the same user facet context as before.
 *
 * User facets can inject user-specific things like {@link tc.oc.api.docs.User}
 * or {@link tc.oc.api.docs.PlayerId}, but not session-specific things like
 * {@link org.bukkit.entity.Player} or {@link MatchPlayer}. Any per-session
 * objects need to be retrieved from events, or looked up through e.g.
 * a {@link MatchPlayerFinder}. And because the facet stays loaded for the
 * entire match, it must never assume that its user is online, or try to
 * use the same per-session objects across seperate events.
 *
 * User facets can use {@link tc.oc.commons.bukkit.event.targeted.TargetedEventHandler}s
 * to receive events for their own user only.
 *
 * User facets are registered with a {@link MatchUserFacetBinder},
 * inside a module that is installed with
 * {@link tc.oc.pgm.match.inject.MatchBinders#installUserModule}.
 *
 * @see MatchUserFacetBinder
 * @see MatchPlayerFacet
 */
public interface MatchUserFacet extends Facet {}
