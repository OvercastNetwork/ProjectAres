package tc.oc.pgm.pickup;

import java.util.Optional;
import javax.annotation.Nullable;

import org.bukkit.entity.EntityType;
import java.time.Duration;
import tc.oc.pgm.features.FeatureDefinition;
import tc.oc.pgm.features.FeatureFactory;
import tc.oc.pgm.features.FeatureInfo;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.kits.Kit;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.regions.Region;

@FeatureInfo(name = "pickup")
public interface PickupDefinition extends FeatureDefinition, FeatureFactory<Pickup> {

    Optional<String> name();

    EntityType appearance();

    Filter visible();

    Filter pickup();

    Region region();

    Kit kit();

    Duration respawn();

    Duration cooldown();

    boolean effects();

    boolean sounds();

}

class PickupDefinitionImpl extends FeatureDefinition.Impl implements PickupDefinition {

    private final @Inspect Optional<String> name;
    private final @Inspect EntityType appearance;
    private final @Inspect Filter visible;
    private final @Inspect Filter pickup;
    private final @Inspect Region region;
    private final @Inspect Kit kit;
    private final @Inspect Duration respawn;
    private final @Inspect Duration cooldown;
    private final @Inspect boolean effects;
    private final @Inspect boolean sounds;

    public PickupDefinitionImpl(@Nullable String name, EntityType appearance, Filter visible, Filter pickup, Region region, Kit kit, Duration respawn, Duration cooldown, boolean effects, boolean sounds) {
        this.name = Optional.ofNullable(name);
        this.appearance = appearance;
        this.visible = visible;
        this.pickup = pickup;
        this.region = region;
        this.kit = kit;
        this.respawn = respawn;
        this.cooldown = cooldown;
        this.effects = effects;
        this.sounds = sounds;
    }

    @Override
    public Optional<String> name() {
        return name;
    }

    @Override
    public EntityType appearance() {
        return appearance;
    }

    @Override
    public Filter visible() {
        return visible;
    }

    @Override
    public Filter pickup() {
        return pickup;
    }

    @Override
    public Region region() {
        return region;
    }

    @Override
    public Kit kit() {
        return kit;
    }

    @Override
    public Duration respawn() {
        return respawn;
    }

    @Override
    public Duration cooldown() {
        return cooldown;
    }

    @Override
    public boolean effects() {
        return effects;
    }

    @Override
    public boolean sounds() {
        return sounds;
    }

    @Override
    public Pickup createFeature(Match match) {
        return new Pickup(match, this);
    }

    @Override
    public void load(Match match) {
        match.features().get(this);
    }
}
