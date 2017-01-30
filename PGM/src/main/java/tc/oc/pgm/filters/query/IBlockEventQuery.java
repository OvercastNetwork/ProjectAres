package tc.oc.pgm.filters.query;

import java.util.Objects;

public interface IBlockEventQuery extends IBlockQuery, IEventQuery {

    @Override
    default int randomSeed() {
        return Objects.hash(getEvent(), getBlock());
    }
}
