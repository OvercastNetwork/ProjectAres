package tc.oc.pgm.match;

import tc.oc.commons.core.inject.Facet;

/**
 * A facet that shares the lifecycle of a single player session and match (i.e. a {@link MatchPlayer}).
 *
 * Player facets are loaded when a player joins the match, and unloaded when they leave the match.
 * If the same player later rejoins the match, they will get a completely new set of facets.
 *
 * Player facets can inject several player-specific things, like {@link tc.oc.api.docs.User},
 * {@link org.bukkit.entity.Player}, or {@link MatchPlayer}.
 *
 * {@link tc.oc.commons.bukkit.event.targeted.TargetedEventHandler}s can be used to receive
 * events only for the respective player.
 *
 * Player facets are registered with a {@link MatchPlayerFacetBinder}, inside a module which
 * must be installed through {@link tc.oc.pgm.match.inject.MatchBinders#installPlayerModule}.
 *
 * @see MatchPlayerFacetBinder
 * @see MatchUserFacet
 */
public interface MatchPlayerFacet extends Facet {}
