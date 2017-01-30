package tc.oc.pgm.match;

import java.util.UUID;

import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import me.anxuiz.settings.SettingManager;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.SimplePlayerId;
import tc.oc.api.docs.User;
import tc.oc.api.docs.UserId;
import tc.oc.commons.bukkit.settings.SettingManagerProvider;
import tc.oc.commons.core.inject.ChildInjectorFactory;
import tc.oc.commons.core.inject.InjectorScoped;
import tc.oc.commons.core.inject.Manifest;
import tc.oc.pgm.match.inject.ForMatchUser;

public class MatchUserManifest extends Manifest {

    private final PlayerId playerId;
    private final UUID uuid;

    MatchUserManifest(User user) {
        this.playerId = SimplePlayerId.copyOf(user);
        this.uuid = user.uuid();
    }

    @Provides PlayerId playerId() { return playerId; }

    @Provides @ForMatchUser UUID uuid() { return uuid; }

    @Override
    protected void configure() {
        bind(MatchUserContext.class).in(InjectorScoped.class);
        install(new MatchFacetContextManifest<>(MatchUserFacet.class, MatchUserContext.class));

        // Bind this explicitly, so it gets the correct parent injector
        bind(new TypeLiteral<ChildInjectorFactory<MatchPlayer>>(){});

        // Eagerly create the PlayerId from the User
        bind(UserId.class).to(PlayerId.class).asEagerSingleton();
    }

    // Bind a dynamic User instance that is always fresh
    @Provides @InjectorScoped
    User user(UserId userId, BukkitUserStore userStore) {
        return userStore.getEverfreshUser(userId);
    }

    @Provides
    SettingManager settingManager(User user, SettingManagerProvider provider) {
        return provider.getManager(user);
    }
}
