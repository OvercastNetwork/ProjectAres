package tc.oc.api.punishments;

import tc.oc.api.docs.Punishment;
import tc.oc.api.docs.virtual.PunishmentDoc;
import tc.oc.api.model.ModelBinders;
import tc.oc.commons.core.inject.HybridManifest;

public class PunishmentModelManifest extends HybridManifest implements ModelBinders {

    @Override
    protected void configure() {
        bindModel(Punishment.class, PunishmentDoc.Partial.class, model -> {
            model.bindDefaultService().to(model.nullService());
        });
    }
}
