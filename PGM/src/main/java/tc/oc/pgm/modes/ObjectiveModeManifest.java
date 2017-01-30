package tc.oc.pgm.modes;

import java.util.List;

import com.google.inject.Provides;
import tc.oc.commons.core.commands.CommandBinder;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.stream.Collectors;
import tc.oc.pgm.features.FeatureBinder;
import tc.oc.pgm.features.MatchFeatureContext;
import tc.oc.pgm.goals.ModeChangeGoal;
import tc.oc.pgm.map.inject.MapBinders;
import tc.oc.pgm.match.inject.MatchBinders;
import tc.oc.pgm.match.inject.MatchScoped;

public class ObjectiveModeManifest extends HybridManifest implements MapBinders, MatchBinders {
    @Override
    protected void configure() {
        new CommandBinder(binder())
            .register(ObjectiveModeCommands.Parent.class);

        final FeatureBinder<ObjectiveMode> modes = new FeatureBinder<>(binder(), ObjectiveMode.class);
        modes.installReflectiveParser();
        modes.installRootParser();

        rootParsers().addBinding().to(ObjectiveModeValidator.class);

        bind(ObjectiveModeManager.class).in(MatchScoped.class);
        matchListener(ObjectiveModeManager.class);
    }

    @Provides
    List<ModeChangeGoal> modeChangeGoals(MatchFeatureContext features) {
        return features.all(ModeChangeGoal.class)
                       .collect(Collectors.toImmutableList());
    }
}
