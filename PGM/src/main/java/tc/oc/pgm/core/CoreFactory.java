package tc.oc.pgm.core;

import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import org.bukkit.material.MaterialData;
import tc.oc.api.docs.virtual.MapDoc;
import tc.oc.pgm.features.FeatureInfo;
import tc.oc.pgm.features.GamemodeFeature;
import tc.oc.pgm.goals.ProximityGoalDefinition;
import tc.oc.pgm.goals.ProximityGoalDefinitionImpl;
import tc.oc.pgm.goals.ProximityMetric;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.regions.Region;
import tc.oc.pgm.teams.TeamFactory;

@FeatureInfo(name = "core")
public interface CoreFactory extends ProximityGoalDefinition, GamemodeFeature {

    @Override
    Core getGoal(Match match);

    Region getRegion();

    MaterialData getMaterial();

    int getLeakLevel();

    boolean hasModeChanges();
}

class CoreFactoryImpl extends ProximityGoalDefinitionImpl implements CoreFactory {
    private final @Inspect Region region;
    private final @Inspect MaterialData material;
    private final @Inspect int leakLevel;
    private final @Inspect boolean modeChanges;

    public CoreFactoryImpl(String name,
                           @Nullable Boolean required,
                           boolean visible,
                           TeamFactory owner,
                           ProximityMetric proximityMetric,
                           Region region,
                           MaterialData material,
                           int leakLevel,
                           boolean modeChanges) {

        super(name, required, visible, Optional.of(owner), proximityMetric);
        this.region = region;
        this.material = material;
        this.leakLevel = leakLevel;
        this.modeChanges = modeChanges;
    }

    @Override
    public Stream<MapDoc.Gamemode> gamemodes() {
        return Stream.of(MapDoc.Gamemode.dtc);
    }

    @Override
    public Core getGoal(Match match) {
        return (Core) super.getGoal(match);
    }

    @Override
    public Core createFeature(Match match) {
        return new Core(this, match);
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
    public MaterialData getMaterial() {
        return this.material;
    }

    @Override
    public int getLeakLevel() {
        return this.leakLevel;
    }

    @Override
    public boolean hasModeChanges() {
        return this.modeChanges;
    }
}
