package tc.oc.pgm.goals;

import javax.annotation.Nullable;

public interface ProximityGoalDefinition<G extends ProximityGoal<?>> extends OwnableGoalDefinition<G> {

    ProximityMetric getPreTouchMetric();

    @Nullable
    ProximityMetric getPostTouchMetric();
}
