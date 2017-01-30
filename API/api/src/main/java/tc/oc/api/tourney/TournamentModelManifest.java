package tc.oc.api.tourney;

import com.google.inject.multibindings.OptionalBinder;
import tc.oc.api.docs.Tournament;
import tc.oc.api.docs.team;
import tc.oc.api.model.ModelBinders;
import tc.oc.commons.core.inject.HybridManifest;

public class TournamentModelManifest extends HybridManifest implements ModelBinders {

    @Override
    protected void configure() {
        bindAndExpose(TournamentStore.class);

        bindModel(team.Team.class, team.Partial.class);

        bindModel(Tournament.class, model -> {
            model.bindStore().to(TournamentStore.class);
            model.queryService().setBinding().to(TournamentService.class);
        });

        OptionalBinder.newOptionalBinder(publicBinder(), TournamentService.class)
                      .setDefault().to(NullTournamentService.class);
    }
}
