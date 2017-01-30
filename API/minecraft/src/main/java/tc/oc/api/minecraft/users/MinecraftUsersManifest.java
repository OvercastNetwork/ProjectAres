package tc.oc.api.minecraft.users;

import com.google.inject.TypeLiteral;
import tc.oc.api.users.UserService;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.minecraft.api.entity.Player;

public class MinecraftUsersManifest extends HybridManifest {

    @Override
    protected void configure() {
        publicBinder().forOptional(UserService.class)
                      .setDefault().to(LocalUserService.class);

        bindAndExpose(new TypeLiteral<UserStore<Player>>(){})
            .to((Class) UserStore.class);

        bindAndExpose(new TypeLiteral<OnlinePlayers<Player>>(){})
            .to((Class) OnlinePlayers.class);
    }
}
