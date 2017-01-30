package tc.oc.pgm.match;

import javax.inject.Inject;

import tc.oc.commons.core.inject.ChildInjectorFactory;

/**
 * This object is not directly useful to anything outside the match plumbing.
 * Don't use it to reference users, just use a UUID or PlayerId or whatever.
 *
 * It should probably be package-local, but currently MatchBinders needs
 * to access it from a different package.
 */
public class MatchUserContext extends MatchFacetContext<MatchUserFacet> {

    final ChildInjectorFactory<MatchPlayer> playerInjectorFactory;

    @Inject MatchUserContext(ChildInjectorFactory<MatchPlayer> playerInjectorFactory) {
        this.playerInjectorFactory = playerInjectorFactory;
    }
}
