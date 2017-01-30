package tc.oc.pgm.teams;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import com.google.inject.Provides;
import tc.oc.commons.core.commands.CommandBinder;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.reflect.TypeLiterals;
import tc.oc.pgm.features.FeatureBinder;
import tc.oc.pgm.map.inject.MapScoped;

public class TeamManifest extends HybridManifest implements TypeLiterals {
    @Override
    protected void configure() {
        installFactory(Team.Factory.class);

        bind(TeamParser.class).in(MapScoped.class);

        final FeatureBinder<TeamFactory> features = new FeatureBinder<>(binder(), TeamFactory.class);
        features.bindParser().to(TeamParser.class);
        features.installReflectiveParser();
        features.installRootParser();

        install(new TeamModule.Factory());

        bind(TeamCommandUtils.class);
        bind(TeamCommands.class);

        new CommandBinder(binder())
            .register(TeamCommands.Parent.class);

        // Tourney needs these
        expose(TeamCommandUtils.class);
        expose(Set(Team.class));
    }

    @Provides
    Set<Team> teams(Optional<TeamMatchModule> teamMatchModule) {
        return teamMatchModule.map(TeamMatchModule::getTeams)
                              .orElse(Collections.emptySet());
    }
}
