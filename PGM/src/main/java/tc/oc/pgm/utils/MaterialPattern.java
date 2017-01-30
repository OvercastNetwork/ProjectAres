package tc.oc.pgm.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import tc.oc.commons.core.inspect.Inspectable;

/**
 * A pattern that matches a specific Material and optionally, its
 * metadata/damage value. If constructed without the data value, the
 * pattern will match only on the Material and ignore the metadata/damage.
 * If constructed with a data value, the pattern will only match materials
 * with that metadata/damage value. In the latter case, Materials passed
 * to the match() method will be assumed to have a data value of 0,
 * and will only match if 0 was also passed to the constructor.
 *
 * The rationale is that only materials that don't use their data value for
 * identity will be passed to the matches() method as Materials, and if the
 * pattern is looking for a non-zero data on such a material, it must be
 * looking for a particular non-default state and thus should not match the
 * default state.
 *
 * TODO: rename MaterialMatcher to MaterialPattern, and this class to something like SingleMaterialPattern
 */

public class MaterialPattern extends Inspectable.Impl implements MaterialMatcher {
    private final @Inspect Material material;
    private final @Inspect Optional<Byte> data;

    public MaterialPattern(Material material, byte data) {
        this.material = material;
        this.data = Optional.of(data);
    }

    public MaterialPattern(MaterialData materialData) {
        this.material = materialData.getItemType();
        this.data = Optional.of(materialData.getData());
    }

    public MaterialPattern(Material material) {
        this.material = material;
        this.data = Optional.empty();
    }

    public Material getMaterial() {
        return this.material;
    }

    @Override
    public Collection<Material> getMaterials() {
        return Collections.singleton(getMaterial());
    }

    public MaterialData getMaterialData() {
        return material.getNewData(data.orElse((byte) 0));
    }

    public boolean dataMatters() {
        return data.isPresent();
    }

    @Override
    public boolean matches(Material material) {
        return material == this.material &&
               (!data.isPresent() || data.get() == 0);
    }

    @Override
    public boolean matches(MaterialData materialData) {
        return materialData.getItemType() == this.material &&
               (!data.isPresent() || data.get() == materialData.getData());
    }

    @Override
    public boolean matches(ItemStack stack) {
        return stack.getType() == this.material &&
               (!data.isPresent() || data.get() == stack.getData().getData());
    }

    public static MaterialPattern accepting(Material material) {
        return new MaterialPattern(material);
    }

    public static MaterialPattern parse(String text) {
        String[] pieces = text.split(":");
        Material material = Material.matchMaterial(pieces[0]);
        if(material == null) {
            throw new IllegalArgumentException("Could not find material '" + pieces[0] + "'.");
        }
        if(pieces.length > 1) {
            try {
                return new MaterialPattern(material, Byte.parseByte(pieces[1]));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid damage value: " + pieces[1], e);
            }
        } else {
            return new MaterialPattern(material);
        }
    }

    public static List<MaterialPattern> fromMaterials(Collection<Material> materials) {
        List<MaterialPattern> patterns = new ArrayList<>(materials.size());
        for(Material material : materials) {
            patterns.add(new MaterialPattern(material));
        }
        return patterns;
    }

    public static List<MaterialPattern> fromMaterialDatas(Collection<MaterialData> materials) {
        List<MaterialPattern> patterns = new ArrayList<>(materials.size());
        for(MaterialData material : materials) {
            patterns.add(new MaterialPattern(material));
        }
        return patterns;
    }
}
