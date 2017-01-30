package tc.oc.pgm.utils;

import java.util.Collection;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import tc.oc.commons.core.inspect.Inspectable;

/**
 * A predicate on materials
 */
public interface MaterialMatcher extends Inspectable {
    boolean matches(Material material);

    boolean matches(MaterialData materialData);

    boolean matches(ItemStack stack);

    /**
     * Iterates over ALL matching {@link Material}s. This can be a long list
     * if the matching criteria is very broad.
     */
    Collection<Material> getMaterials();
}
