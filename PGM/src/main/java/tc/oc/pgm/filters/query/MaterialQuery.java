package tc.oc.pgm.filters.query;

import com.google.common.cache.LoadingCache;
import org.bukkit.material.MaterialData;
import tc.oc.commons.core.util.CacheUtils;

import static com.google.common.base.Preconditions.checkNotNull;

public class MaterialQuery implements IMaterialQuery {

    private final MaterialData material;

    private MaterialQuery(MaterialData material) {
        this.material = checkNotNull(material);
    }

    @Override
    public MaterialData getMaterial() {
        return material;
    }

    private static final LoadingCache<MaterialData, MaterialQuery> CACHE = CacheUtils.newCache(MaterialQuery::new);

    public static MaterialQuery of(MaterialData material) {
        return CACHE.getUnchecked(material);
    }
}
