package tc.oc.api.sessions;

import com.google.inject.multibindings.OptionalBinder;
import tc.oc.api.docs.Session;
import tc.oc.api.docs.virtual.SessionDoc;
import tc.oc.api.model.ModelBinders;
import tc.oc.commons.core.inject.HybridManifest;

public class SessionModelManifest extends HybridManifest implements ModelBinders {

    @Override
    protected void configure() {
        bindModel(Session.class, SessionDoc.Partial.class, model -> {
            model.bindService().to(SessionService.class);
        });

        OptionalBinder.newOptionalBinder(publicBinder(), SessionService.class);
    }
}
