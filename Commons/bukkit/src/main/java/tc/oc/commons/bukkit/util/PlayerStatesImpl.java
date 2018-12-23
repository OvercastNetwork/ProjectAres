package tc.oc.commons.bukkit.util;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

@Singleton
public class PlayerStatesImpl implements PlayerStates {

    private final static String DEAD_KEY = "isDead";
    private final static String PARTICIPATING_KEY = "isParticipating";

    private final Plugin plugin;

    @Inject
    PlayerStatesImpl(Plugin plugin) {
        this.plugin = plugin;
    }

    private boolean get(Player player, String key, boolean fallback) {
        final MetadataValue value = player.getMetadata(key, plugin);
        return value != null ? value.asBoolean() : fallback;
    }

    private void set(Player player, String key, @Nullable Boolean value) {
        if(value != null) {
            player.setMetadata(key, new FixedMetadataValue(plugin, value));
        } else {
            player.removeMetadata(key, plugin);
        }
    }

    @Override
    public boolean isDead(Player player) {
        return get(player, DEAD_KEY, player.isDead());
    }

    @Override
    public void setDead(Player player, @Nullable Boolean dead) {
        set(player, DEAD_KEY, dead);
    }

    @Override
    public boolean isParticipating(Player player) {
        return get(player, PARTICIPATING_KEY, player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE);
    }

    @Override
    public void setParticipating(Player player, @Nullable Boolean value) {
        set(player, PARTICIPATING_KEY, value);
    }

}
