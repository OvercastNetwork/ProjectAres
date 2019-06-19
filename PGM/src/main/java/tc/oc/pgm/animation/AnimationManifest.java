package tc.oc.pgm.animation;

import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.pgm.map.inject.MapBinders;
import tc.oc.pgm.map.inject.MapScoped;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.inject.MatchBinders;
import tc.oc.pgm.match.inject.MatchScoped;

public class AnimationManifest extends HybridManifest implements MapBinders, MatchBinders {
    @Override
    protected void configure() {
        installInnerClassFactory(AnimationDefinitionImpl.AnimationImpl.class);
        installFactory(AnimationDefinitionImpl.Factory.class);

        bind(AnimationParser.class).in(MapScoped.class);
        rootParsers().addBinding().to(AnimationParser.class);

        bind(AnimationScheduler.class).in(MatchScoped.class);
        matchListener(AnimationScheduler.class, MatchScope.LOADED);
    }
}
