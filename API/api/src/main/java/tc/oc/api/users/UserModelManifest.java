package tc.oc.api.users;

import com.google.inject.multibindings.OptionalBinder;
import tc.oc.api.docs.User;
import tc.oc.api.docs.virtual.UserDoc;
import tc.oc.api.model.ModelBinders;
import tc.oc.commons.core.inject.HybridManifest;

public class UserModelManifest extends HybridManifest implements ModelBinders {

    @Override
    protected void configure() {
        bindModel(User.class, UserDoc.Partial.class, model -> {
            model.bindService().to(UserService.class);
        });

        OptionalBinder.newOptionalBinder(publicBinder(), UserService.class);
    }
}
