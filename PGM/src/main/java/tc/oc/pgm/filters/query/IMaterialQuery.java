package tc.oc.pgm.filters.query;

import org.bukkit.material.MaterialData;

public interface IMaterialQuery extends IQuery {

    MaterialData getMaterial();

    @Override
    default int randomSeed() {
        return getMaterial().hashCode();
    }
}
