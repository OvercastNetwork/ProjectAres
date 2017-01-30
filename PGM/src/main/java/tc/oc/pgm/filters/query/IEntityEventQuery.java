package tc.oc.pgm.filters.query;

import java.util.Objects;

public interface IEntityEventQuery extends IEventQuery, IEntityTypeQuery {
    @Override
    default int randomSeed() {
        return Objects.hash(getEvent(), getEntityType());
    }
}
