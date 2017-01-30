package tc.oc.pgm.filters.query;

import java.util.Objects;

public interface IPlayerBlockEventQuery extends IPlayerEventQuery, IBlockEventQuery {

    @Override
    default int randomSeed() {
        return Objects.hash(getEvent(), getPlayerId(), getBlock());
    }
}
