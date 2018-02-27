package tc.oc.pgm.beacon;

import org.bukkit.DyeColor;
import org.bukkit.util.Vector;
import tc.oc.pgm.features.FeatureDefinition;
import tc.oc.pgm.features.FeatureFactory;
import tc.oc.pgm.features.FeatureInfo;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.module.ModuleLoadException;

@FeatureInfo(name = "beacon")
public interface BeaconDefinition extends FeatureDefinition, FeatureFactory<Beacon> {

    Filter visible();

    Integer particleCount();

    Vector location();

    DyeColor color();

}

class BeaconDefinitionImpl extends FeatureDefinition.Impl implements BeaconDefinition {

    private final @Inspect Filter visible;
    private final @Inspect Integer particleCount;
    private final @Inspect Vector location;
    private final @Inspect DyeColor color;

    public BeaconDefinitionImpl(Filter visible, Integer particleCount, Vector location, DyeColor color) {
        this.visible = visible;
        this.particleCount = particleCount;
        this.location = location;
        this.color = color;
    }

    @Override
    public Filter visible() {
        return visible;
    }

    @Override
    public Integer particleCount() {
        return particleCount;
    }

    @Override
    public Vector location() {
        return location;
    }

    @Override
    public DyeColor color() {
        return color;
    }

    @Override
    public Beacon createFeature(Match match) throws ModuleLoadException {
        return new Beacon(match, this);
    }

    @Override
    public void load(Match match) {
        match.features().get(this);
    }

}
