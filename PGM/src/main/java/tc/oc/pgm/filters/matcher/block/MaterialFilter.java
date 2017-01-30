package tc.oc.pgm.filters.matcher.block;

import org.bukkit.Material;
import org.bukkit.material.MaterialData;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.matcher.TypedFilter;
import tc.oc.pgm.filters.query.IMaterialQuery;
import tc.oc.pgm.utils.MaterialPattern;

public class MaterialFilter extends TypedFilter.Impl<IMaterialQuery> {
    private final @Inspect(inline=true) MaterialPattern pattern;

    public MaterialFilter(MaterialData materialData) {
        this(new MaterialPattern(materialData));
    }

    public MaterialFilter(Material material) {
        this(new MaterialPattern(material));
    }

    public MaterialFilter(MaterialPattern pattern) {
        this.pattern = pattern;
    }

    public static Filter of(MaterialPattern pattern) {
        return new MaterialFilter(pattern);
    }

    @Override
    public boolean matches(IMaterialQuery query) {
        return pattern.matches(query.getMaterial());
    }
}
