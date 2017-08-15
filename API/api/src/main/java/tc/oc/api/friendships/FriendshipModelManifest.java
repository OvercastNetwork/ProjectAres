package tc.oc.api.friendships;

import com.google.inject.multibindings.OptionalBinder;
import tc.oc.api.docs.Friendship;
import tc.oc.api.docs.virtual.FriendshipDoc;
import tc.oc.api.model.ModelBinders;
import tc.oc.commons.core.inject.HybridManifest;

public class FriendshipModelManifest extends HybridManifest implements ModelBinders {

    @Override
    protected void configure() {
        bindModel(Friendship.class, FriendshipDoc.Partial.class, model -> {
            model.bindService().to(FriendshipService.class);
        });

        OptionalBinder.newOptionalBinder(publicBinder(), FriendshipService.class)
                .setDefault().to(NullFriendshipService.class);
    }
}
