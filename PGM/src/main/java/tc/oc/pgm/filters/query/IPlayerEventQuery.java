package tc.oc.pgm.filters.query;

import java.util.Objects;

public interface IPlayerEventQuery extends IPlayerQuery, IEventQuery {

    @Override
    default int randomSeed() {
        return Objects.hash(getEvent(), getPlayerId());
    }
}
