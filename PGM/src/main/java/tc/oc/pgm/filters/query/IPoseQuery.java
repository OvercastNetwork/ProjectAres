package tc.oc.pgm.filters.query;

import java.util.Set;

import org.bukkit.PoseFlag;

public interface IPoseQuery extends IMatchQuery {

    Set<PoseFlag> getPose();

    @Override
    default int randomSeed() {
        return getPose().hashCode();
    }
}
