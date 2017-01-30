package tc.oc.commons.bukkit.localization;

import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.material.MaterialData;

public interface BukkitTranslator {

    Optional<String> materialKey(Material material);

    Optional<String> materialKey(MaterialData material);
}
