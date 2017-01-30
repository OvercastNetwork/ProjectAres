package tc.oc.pgm.goals;

import java.util.Optional;

import tc.oc.pgm.teams.TeamFactory;

import javax.annotation.Nullable;

public abstract class ProximityGoalDefinitionImpl<G extends ProximityGoal<?>> extends OwnableGoalDefinitionImpl<G> implements ProximityGoalDefinition<G> {
    private final @Inspect ProximityMetric preTouchMetric;
    private final @Inspect @Nullable ProximityMetric postTouchMetric;

    public ProximityGoalDefinitionImpl(String name, @Nullable Boolean required, boolean visible, Optional<TeamFactory> owner, ProximityMetric preTouchMetric, @Nullable ProximityMetric postTouchMetric) {
        super(name, required, visible, owner);
        this.preTouchMetric = preTouchMetric;
        this.postTouchMetric = postTouchMetric;
    }

    public ProximityGoalDefinitionImpl(String name, @Nullable Boolean required, boolean visible, Optional<TeamFactory> owner, ProximityMetric preTouchMetric) {
        this(name, required, visible, owner, preTouchMetric, null);
    }

    @Override
    public ProximityMetric getPreTouchMetric() {
        return this.preTouchMetric;
    }

    @Override
    public @Nullable ProximityMetric getPostTouchMetric() {
        return postTouchMetric;
    }
}
