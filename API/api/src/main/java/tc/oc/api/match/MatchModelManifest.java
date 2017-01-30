package tc.oc.api.match;

import tc.oc.api.docs.Death;
import tc.oc.api.docs.Objective;
import tc.oc.api.docs.Participation;
import tc.oc.api.docs.virtual.DeathDoc;
import tc.oc.api.docs.virtual.MatchDoc;
import tc.oc.api.model.ModelBinders;
import tc.oc.commons.core.inject.HybridManifest;

public class MatchModelManifest extends HybridManifest implements ModelBinders {

    @Override
    protected void configure() {
        bindModel(MatchDoc.class, model -> {
            model.bindDefaultService().to(model.nullService());
        });

        bindModel(Participation.Complete.class, Participation.Partial.class, model -> {
            model.bindDefaultService().to(model.nullService());
        });

        bindModel(Death.class, DeathDoc.Partial.class, model -> {
            model.bindDefaultService().to(model.nullService());
        });

        bindModel(Objective.class, model -> {
            model.bindDefaultService().to(model.nullService());
        });
    }
}
