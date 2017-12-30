package tc.oc.pgm.control.point;

import java.time.Duration;
import java.util.Optional;
import javax.annotation.Nullable;

import tc.oc.pgm.control.ControllableGoalDefinition;
import tc.oc.pgm.control.ControllableGoalDefinitionImpl;
import tc.oc.pgm.features.Feature;
import tc.oc.pgm.features.FeatureInfo;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.module.ModuleLoadException;
import tc.oc.pgm.regions.Region;
import tc.oc.pgm.teams.TeamFactory;

@FeatureInfo(name = "control-point",
             plural = {"control-points", "hills"},
             singular = {"control-point", "hill"})
public interface ControlPointDefinition extends ControllableGoalDefinition {

    @Override
    ControlPoint getGoal(Match match);

    Region captureRegion();

    Region progressDisplayRegion();

    Region controllerDisplayRegion();

    Filter visualMaterials();

}

class ControlPointDefinitionImpl extends ControllableGoalDefinitionImpl implements ControlPointDefinition {

    private final Region captureRegion;
    private final Region progressDisplayRegion;
    private final Region controllerDisplayRegion;
    private final Filter visualMaterials;

    public ControlPointDefinitionImpl(String name, @Nullable Boolean required, boolean visible, Filter captureFilter, Filter defendFilter, Duration captureTime, double multiplierTime, double recoveryRate, double decayRate, Optional<TeamFactory> initialOwner, CaptureCondition captureCondition, boolean neutralState, boolean permanent, float pointsOwned, float pointsPerSecond, float pointsGrowth, boolean showProgress,
                                      Region captureRegion,
                                      Region progressDisplayRegion,
                                      Region controllerDisplayRegion,
                                      Filter visualMaterials) {
        super(name, required, visible, captureFilter, defendFilter, captureTime, multiplierTime, recoveryRate, decayRate, initialOwner, captureCondition, neutralState, permanent, pointsOwned, pointsPerSecond, pointsGrowth, showProgress);
        this.captureRegion = captureRegion;
        this.progressDisplayRegion = progressDisplayRegion;
        this.controllerDisplayRegion = controllerDisplayRegion;
        this.visualMaterials = visualMaterials;
    }

    @Override
    public Region captureRegion() {
        return captureRegion;
    }

    @Override
    public Region progressDisplayRegion() {
        return progressDisplayRegion;
    }

    @Override
    public Region controllerDisplayRegion() {
        return controllerDisplayRegion;
    }

    @Override
    public Filter visualMaterials() {
        return visualMaterials;
    }

    @Override
    public ControlPoint getGoal(Match match) {
        return (ControlPoint) super.getGoal(match);
    }

    @Override
    public Feature<?> createFeature(Match match) throws ModuleLoadException {
        return new ControlPoint(match, this);
    }

}
