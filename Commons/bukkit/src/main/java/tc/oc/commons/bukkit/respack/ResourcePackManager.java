package tc.oc.commons.bukkit.respack;

import javax.annotation.Nullable;
import org.bukkit.entity.Player;

public interface ResourcePackManager {

    boolean isEnabled();

    void setEnabled(boolean enabled);

    boolean isFastUpdate();

    @Nullable String getUrl();

    @Nullable String getSha1();

    /**
     * Send the latest resource pack to the given player, if they don't already have it.
     */
    void refreshPlayer(Player player);

    /**
     * Send the latest resource pack to all online players that don't already have it.
     */
    void refreshAll();
}
