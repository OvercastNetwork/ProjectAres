package tc.oc.api.trophies;

import tc.oc.api.docs.Trophy;
import tc.oc.api.model.ModelBinders;
import tc.oc.commons.core.inject.HybridManifest;

public class TrophyModelManifest extends HybridManifest implements ModelBinders {

    @Override
    protected void configure() {
        bindAndExpose(TrophyStore.class);

        bindModel(Trophy.class, model -> {
            model.bindStore().to(TrophyStore.class);
            model.queryService().setDefault().to(model.nullQueryService());
        });
    }
}
