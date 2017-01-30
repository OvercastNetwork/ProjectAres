package tc.oc.pgm.utils;

import java.util.Arrays;
import java.util.Collection;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.util.ImmutableMaterialSet;
import tc.oc.commons.core.inspect.Inspectable;

public class BlockMaterialMatcher extends Inspectable.Impl implements MaterialMatcher {

    public static final BlockMaterialMatcher INSTANCE = new BlockMaterialMatcher();

    private BlockMaterialMatcher() {}

    private static final ImmutableMaterialSet BLOCKS = ImmutableMaterialSet.of(Collections2.filter(Arrays.asList(Material.values()), new Predicate<Material>() {
        @Override
        public boolean apply(Material input) {
            return input.isBlock();
        }
    }));

    @Override
    public boolean matches(Material material) {
        return material.isBlock();
    }

    @Override
    public boolean matches(MaterialData materialData) {
        return materialData.getItemType().isBlock();
    }

    @Override
    public boolean matches(ItemStack stack) {
        return stack.getType().isBlock();
    }

    @Override
    public Collection<Material> getMaterials() {
        return BLOCKS;
    }
}
