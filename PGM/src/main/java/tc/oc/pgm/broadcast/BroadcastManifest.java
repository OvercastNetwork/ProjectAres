package tc.oc.pgm.broadcast;

import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.pgm.features.FeatureBinder;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.inject.MatchBinders;
import tc.oc.pgm.match.inject.MatchScoped;
import tc.oc.pgm.xml.parser.EnumParserManifest;

public class BroadcastManifest extends HybridManifest implements MatchBinders {
    @Override
    protected void configure() {
        install(new EnumParserManifest<Broadcast.Type>(){});

        final FeatureBinder<Broadcast> feature = new FeatureBinder<>(binder(), Broadcast.class);
        feature.bindDefinitionParser().to(BroadcastParser.class);
        feature.installRootParser();

        bind(BroadcastScheduler.class).in(MatchScoped.class);
        matchListener(BroadcastScheduler.class, MatchScope.LOADED);
    }
}
