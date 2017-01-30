package tc.oc.lobby.bukkit.gizmos.empty;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import tc.oc.lobby.bukkit.LobbyTranslations;
import tc.oc.lobby.bukkit.gizmos.Gizmo;

public class EmptyGizmo extends Gizmo {
    public EmptyGizmo(String name, String prefix, String description, Material icon, int cost) {
        super(name, prefix, description, icon, cost);
    }

    @Override
    public String getName(Player viewer) {
        return LobbyTranslations.get().t("gizmo.empty.name", viewer);
    }

    @Override
    public String getDescription(Player viewer) {
        return LobbyTranslations.get().t("gizmo.empty.description", viewer);
    }

    @Override
    protected void initialize() {

    }
}
