package tc.oc.pgm.control;

import tc.oc.api.docs.virtual.MapDoc;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.goals.GoalDefinitionImpl;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.teams.TeamFactory;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class ControllableGoalDefinitionImpl extends GoalDefinitionImpl implements ControllableGoalDefinition {

    private final Filter captureFilter;
    private final Filter defendFilter;
    private final Duration captureTime;
    private final double multiplierTime;
    private final double recoveryRate;
    private final double decayRate;
    private final Optional<TeamFactory> initialOwner;
    private final CaptureCondition captureCondition;
    private final boolean neutralState;
    private final boolean permanent;
    private final float pointsOwned;
    private final float pointsPerSecond;
    private final float pointsGrowth;
    private final boolean showProgress;

    public ControllableGoalDefinitionImpl(String name,
                                          @Nullable Boolean required,
                                          boolean visible,
                                          Filter captureFilter,
                                          Filter defendFilter,
                                          Duration captureTime,
                                          double multiplierTime,
                                          double recoveryRate,
                                          double decayRate,
                                          Optional<TeamFactory> initialOwner,
                                          CaptureCondition captureCondition,
                                          boolean neutralState,
                                          boolean permanent,
                                          float pointsOwned,
                                          float pointsPerSecond,
                                          float pointsGrowth,
                                          boolean showProgress) {
        super(name, required, visible);
        this.captureFilter = captureFilter;
        this.defendFilter = defendFilter;
        this.captureTime = captureTime;
        this.multiplierTime = multiplierTime;
        this.recoveryRate = recoveryRate;
        this.decayRate = decayRate;
        this.initialOwner = initialOwner;
        this.captureCondition = captureCondition;
        this.neutralState = neutralState;
        this.permanent = permanent;
        this.pointsOwned = pointsOwned;
        this.pointsPerSecond = pointsPerSecond;
        this.pointsGrowth = pointsGrowth;
        this.showProgress = showProgress;
    }

    @Override
    public Stream<MapDoc.Gamemode> gamemodes() {
        return Stream.of(MapDoc.Gamemode.koth);
    }

    @Override
    public boolean isShared() {
        return true;
    }

    @Override
    public Filter captureFilter() {
        return captureFilter;
    }

    @Override
    public Filter defendFilter() {
        return defendFilter;
    }

    @Override
    public Duration captureTime() {
        return captureTime;
    }

    @Override
    public double multiplierTime() {
        return multiplierTime;
    }

    @Override
    public double decayRate() {
        return decayRate;
    }

    @Override
    public double recoveryRate() {
        return recoveryRate;
    }

    @Override
    public Optional<TeamFactory> initialOwner() {
        return initialOwner;
    }

    @Override
    public CaptureCondition captureCondition() {
        return captureCondition;
    }

    @Override
    public boolean neutralState() {
        return neutralState;
    }

    @Override
    public boolean permanent() {
        return permanent;
    }

    @Override
    public float pointsOwned() {
        return pointsOwned;
    }

    @Override
    public float pointsPerSecond() {
        return pointsPerSecond;
    }

    @Override
    public float pointsGrowth() {
        return pointsGrowth;
    }

    @Override
    public boolean showProgress() {
        return showProgress;
    }

}
