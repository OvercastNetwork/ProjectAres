package net.anxuiz.tourney;

import java.util.Optional;
import javax.inject.Singleton;

import com.google.inject.Provides;
import net.anxuiz.tourney.command.TourneyCommands;
import org.bukkit.plugin.Plugin;
import tc.oc.api.docs.Tournament;
import tc.oc.api.tourney.TournamentStore;
import tc.oc.commons.core.commands.CommandBinder;
import tc.oc.commons.core.inject.HybridManifest;

public class TourneyManifest extends HybridManifest {
    @Override
    protected void configure() {
        new CommandBinder(binder())
            .register(TourneyCommands.TourneyParentCommand.class);
    }

    @Provides
    Tourney tourney(Plugin plugin) {
        return (Tourney) plugin;
    }

    @Provides @Singleton
    Tournament tournament(TournamentStore tournamentStore) {
        return tournamentStore.proxy(Config.tournamentID());
    }

    @Provides
    Optional<ReadyManager> readyManager(MatchManager matchManager) {
        return Optional.ofNullable(matchManager.getReadyManager());
    }
}
