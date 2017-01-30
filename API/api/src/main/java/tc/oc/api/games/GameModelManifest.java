package tc.oc.api.games;

import tc.oc.api.docs.Arena;
import tc.oc.api.docs.Game;
import tc.oc.api.docs.Ticket;
import tc.oc.api.model.ModelBinders;
import tc.oc.commons.core.inject.HybridManifest;

public class GameModelManifest extends HybridManifest implements ModelBinders {

    @Override
    protected void configure() {
        bindAndExpose(GameStore.class);
        bindAndExpose(ArenaStore.class);
        bindAndExpose(TicketStore.class);

        bindModel(Game.class, model -> {
            model.bindStore().to(GameStore.class);
            model.queryService().setDefault().to(model.nullQueryService());
        });

        bindModel(Arena.class, model -> {
            model.bindStore().to(ArenaStore.class);
            model.queryService().setDefault().to(model.nullQueryService());
        });

        bindModel(Ticket.class, model -> {
            model.bindStore().to(TicketStore.class);
            model.queryService().setBinding().to(TicketService.class);
        });

        publicBinder().forOptional(TicketService.class)
                      .setDefault().to(NullTicketService.class);
    }
}
