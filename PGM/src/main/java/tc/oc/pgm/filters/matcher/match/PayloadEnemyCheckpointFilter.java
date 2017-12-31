package tc.oc.pgm.filters.matcher.match;

import com.google.common.collect.Range;
import tc.oc.pgm.filters.matcher.TypedFilter;
import tc.oc.pgm.filters.query.IMatchQuery;
import tc.oc.pgm.payload.Payload;
import tc.oc.pgm.payload.PayloadDefinition;

public class PayloadEnemyCheckpointFilter extends TypedFilter.Impl<IMatchQuery> {

    private final @Inspect(brief=true) PayloadDefinition payload;
    private final @Inspect(brief=true) Range<Integer> checkpointRange;

    public PayloadEnemyCheckpointFilter(PayloadDefinition payload, Range<Integer> checkpointRange) {
        this.payload = payload;
        this.checkpointRange = checkpointRange;
    }

    @Override
    public String inspectType() {
        return "PayloadCheckpoint";
    }

    @Override
    public String toString() {
        return inspect();
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public boolean matches(IMatchQuery query) {
        Payload p = query.feature(payload);

        for (int i = checkpointRange.lowerEndpoint(); i <= checkpointRange.upperEndpoint(); i++) {
            if (p.enemyReachedCheckpoints.contains(p.allCheckpoints.get(i))) {
                return true;
            }
        }
        return false;
    }
}
