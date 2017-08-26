package tc.oc.commons.bukkit.flairs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.ImmutableSet;
import net.md_5.bungee.api.chat.BaseComponent;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.docs.virtual.UserDoc;
import tc.oc.api.minecraft.MinecraftService;
import tc.oc.commons.bukkit.chat.NameFlag;
import tc.oc.commons.bukkit.chat.NameType;
import tc.oc.commons.bukkit.chat.PartialNameRenderer;
import tc.oc.commons.bukkit.nick.Identity;
import tc.oc.commons.core.chat.Components;

/**
 * Renders a player's flair only
 */
@Singleton
public class FlairRenderer implements PartialNameRenderer {

    private final MinecraftService minecraftService;
    private final BukkitUserStore userStore;
    private final FlairConfiguration flairConfiguration;

    @Inject protected FlairRenderer(MinecraftService minecraftService, BukkitUserStore userStore, FlairConfiguration flairConfiguration) {
        this.minecraftService = minecraftService;
        this.userStore = userStore;
        this.flairConfiguration = flairConfiguration;
    }

    @Override
    public String getLegacyName(Identity identity, NameType type) {
        if(!(type.style.contains(NameFlag.FLAIR) && type.reveal)) return "";
        return getFlairs(identity).reduce("", String::concat);
    }

    @Override
    public BaseComponent getComponentName(Identity identity, NameType type) {
        return Components.fromLegacyText(getLegacyName(identity, type));
    }

    public Stream<String> getFlairs(Identity identity) {
        final UserDoc.Identity user;
        if(identity.getPlayerId() instanceof UserDoc.Identity) {
            // Flair may already be stashed inside the Identity
            user = (UserDoc.Identity) identity.getPlayerId();
        } else {
            user = userStore.tryUser(identity.getPlayerId());
        }
        if(user == null) return Stream.empty();

        final Set<String> realms = ImmutableSet.copyOf(minecraftService.getLocalServer().realms());

        return user.minecraft_flair()
                .stream()
                .filter(flair -> realms.contains(flair.realm))
                .sorted((flair1, flair2) -> flair1.priority - flair2.priority)
                .limit(flairConfiguration.maxFlairs() < 0 ? Long.MAX_VALUE : flairConfiguration.maxFlairs())
                .sorted((flair1, flair2) -> flair2.priority - flair1.priority)
                .map(flair -> flair.text);
    }

    public int getNumberOfFlairs(Identity identity) {
        return (int) getFlairs(identity).count();
    }

}
