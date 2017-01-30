package tc.oc.pgm.utils;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import tc.oc.commons.core.inspect.Inspectable;

/**
 * Matches all materials
 */
public class AllMaterialMatcher extends Inspectable.Impl implements MaterialMatcher {

    public static final AllMaterialMatcher INSTANCE = new AllMaterialMatcher();

    private AllMaterialMatcher() {}

    @Override
    public boolean matches(Material material) {
        return true;
    }

    @Override
    public boolean matches(MaterialData materialData) {
        return true;
    }

    @Override
    public boolean matches(ItemStack stack) {
        return true;
    }

    @Override
    public Collection<Material> getMaterials() {
        return Arrays.asList(Material.values());
    }
}
