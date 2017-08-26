package tc.oc.commons.bukkit.tablist;

import java.util.UUID;
import javax.annotation.Nullable;
import javax.inject.Inject;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Skin;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerSkinPartsChangeEvent;
import tc.oc.commons.bukkit.chat.NameStyle;
import tc.oc.commons.bukkit.chat.PlayerComponent;
import tc.oc.commons.bukkit.nick.Identity;
import tc.oc.commons.bukkit.nick.IdentityProvider;
import tc.oc.commons.bukkit.nick.PlayerIdentityChangeEvent;
import tc.oc.commons.core.util.DefaultProvider;

/**
 * {@link TabEntry} showing a {@link Player}'s name and skin.
 *
 * Note that this is NOT the player's real entry. It has a random UUID and name,
 * like any other {@link SimpleTabEntry}. While this entry is visible in a {@link TabView},
 * a fake player entity will be spawned with a copy of the real player's metadata.
 */
public class PlayerTabEntry extends DynamicTabEntry {
    public static class Factory implements DefaultProvider<Player, PlayerTabEntry> {
        @Override
        public PlayerTabEntry get(Player key) {
            return new PlayerTabEntry(key);
        }
    }

    private static UUID randomUUIDVersion2SameDefaultSkin(UUID original) {
        // Parity of UUID.hashCode determines if the player's default skin is Steve/Alex
        // To make the player list match, we generate a random UUID with the same hashCode parity.
        // UUID.hashCode returns the XOR of its four 32-bit segments, so set bit 0 to the desired
        // parity, and clear bits 32, 64, and 96

        long parity = original.hashCode() & 1L;
        long mask = ~((1L << 32) | 1L);
        UUID uuid = randomUUIDVersion2();
        uuid = new UUID(uuid.getMostSignificantBits() & mask, (uuid.getLeastSignificantBits() & mask) | parity);
        return uuid;
    }

    @Inject private static IdentityProvider identityProvider;

    protected final Player player;
    protected @Nullable PlayerComponent content;
    private final int spareEntityId;

    public PlayerTabEntry(Player player) {
        super(randomUUIDVersion2SameDefaultSkin(player.getUniqueId()));
        this.player = player;
        this.spareEntityId = player.getServer().allocateEntityId();
    }

    @Override
    public BaseComponent getContent(TabView view) {
        if(content == null) {
            this.content = new PlayerComponent(identityProvider.currentIdentity(player), NameStyle.GAME);
        }
        return content;
    }

    @Override
    public int getFakeEntityId(TabView view) {
        return this.spareEntityId;
    }

    @Override
    public Player getFakePlayer(TabView view) {
        return this.player;
    }

    @Override
    public @Nullable Skin getSkin(TabView view) {
        final Identity identity = identityProvider.currentIdentity(player);
        return identity.isDisguised(view.getViewer()) ? player.getFakeSkin(view.getViewer()) : player.getSkin();
    }

    // Dispatched by TabManager
    protected void onNickChange(PlayerIdentityChangeEvent event) {
        if(this.player == event.getPlayer()) {
            // PlayerComponents are bound to an Identity, and we always want to show
            // the player's current Identity, so we have to replace the PlayerComponent
            // when the player's identity changes.
            this.content = null;
            this.invalidate();
            this.refresh();
        }
    }

    // Dispatched by TabManager
    protected void onSkinPartsChange(PlayerSkinPartsChangeEvent event) {
        if(this.player == event.getPlayer()) {
            this.updateFakeEntity();
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" + this.player.getName() + "}";
    }
}
