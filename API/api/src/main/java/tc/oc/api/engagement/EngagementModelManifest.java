package tc.oc.api.engagement;

import com.google.inject.multibindings.OptionalBinder;
import tc.oc.api.docs.virtual.EngagementDoc;
import tc.oc.api.model.ModelBinders;
import tc.oc.commons.core.inject.HybridManifest;

public class EngagementModelManifest extends HybridManifest implements ModelBinders {

    @Override
    protected void configure() {
        bindModel(EngagementDoc.class);

        OptionalBinder.newOptionalBinder(publicBinder(), EngagementService.class)
                      .setDefault().to(LocalEngagementService.class);
    }
}
