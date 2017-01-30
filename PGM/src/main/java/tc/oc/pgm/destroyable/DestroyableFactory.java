package tc.oc.pgm.destroyable;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import tc.oc.api.docs.virtual.MapDoc;
import tc.oc.pgm.features.FeatureInfo;
import tc.oc.pgm.features.GamemodeFeature;
import tc.oc.pgm.goals.ProximityGoalDefinition;
import tc.oc.pgm.goals.ProximityGoalDefinitionImpl;
import tc.oc.pgm.goals.ProximityMetric;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.regions.Region;
import tc.oc.pgm.teams.TeamFactory;
import tc.oc.pgm.utils.MaterialPattern;

@FeatureInfo(name = "destroyable",
             plural = {"destroyables", "giraffes"},
             singular = {"destroyable", "giraffe"})

public interface DestroyableFactory extends ProximityGoalDefinition, GamemodeFeature {

    @Override
    Destroyable getGoal(Match match);

    Region getRegion();

    Set<MaterialPattern> getMaterials();

    double getDestructionRequired();

    boolean hasModeChanges();

    boolean getShowProgress();

    boolean hasSparks();

    boolean isRepairable();
}

class DestroyableFactoryImpl extends ProximityGoalDefinitionImpl implements DestroyableFactory {
    private final @Inspect Region region;
    private final @Inspect Set<MaterialPattern> materials;
    private final @Inspect double destructionRequired;
    private final @Inspect boolean modeChanges;
    private final @Inspect boolean showProgress;
    private final @Inspect boolean sparks;
    private final @Inspect boolean repairable;

    public DestroyableFactoryImpl(String name,
                                  @Nullable Boolean required,
                                  boolean visible,
                                  TeamFactory owner,
                                  ProximityMetric proximityMetric,
                                  Region region,
                                  Set<MaterialPattern> materials,
                                  double destructionRequired,
                                  boolean modeChanges,
                                  boolean showProgress,
                                  boolean sparks,
                                  boolean repairable) {
        super(name, required, visible, Optional.of(owner), proximityMetric);
        this.region = region;
        this.materials = materials;
        this.destructionRequired = destructionRequired;
        this.modeChanges = modeChanges;
        this.showProgress = showProgress;
        this.sparks = sparks;
        this.repairable = repairable;
    }

    @Override
    public Stream<MapDoc.Gamemode> gamemodes() {
        return Stream.of(MapDoc.Gamemode.dtm);
    }

    @Override
    public Destroyable getGoal(Match match) {
        return (Destroyable) super.getGoal(match);
    }

    @Override
    public Destroyable createFeature(Match match) {
        return new Destroyable(this, match);
    }

    @Override
    public boolean isShared() {
        return false;
    }

    @Override
    public Region getRegion() {
        return this.region;
    }

    @Override
    public Set<MaterialPattern> getMaterials() {
        return this.materials;
    }

    @Override
    public double getDestructionRequired() {
        return this.destructionRequired;
    }

    @Override
    public boolean hasModeChanges() {
        return this.modeChanges;
    }

    @Override
    public boolean getShowProgress() {
        return this.showProgress;
    }

    @Override
    public boolean hasSparks() {
        return this.sparks;
    }

    @Override
    public boolean isRepairable() {
        return this.repairable;
    }
}
