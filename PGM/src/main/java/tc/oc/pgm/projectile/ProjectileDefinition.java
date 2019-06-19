package tc.oc.pgm.projectile;

import java.util.List;
import javax.annotation.Nullable;

import com.google.inject.Inject;
import org.bukkit.entity.Entity;
import org.bukkit.potion.PotionEffect;
import java.time.Duration;
import tc.oc.pgm.features.FeatureDefinition;
import tc.oc.pgm.features.FeatureInfo;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.kits.Kit;

@FeatureInfo(name = "projectile")
public interface ProjectileDefinition extends FeatureDefinition {

    @Nullable String getName();

    @Nullable Double damage();

    double velocity();

    ClickAction clickAction();

    Class<? extends Entity> projectile();

    List<PotionEffect> potion();

    Filter destroyFilter();

    Duration cooldown();

    boolean throwable();

    Kit victimKit();

    Kit attackerKit();
}

class ProjectileDefinitionImpl extends FeatureDefinition.Impl implements ProjectileDefinition {
    private @Inspect @Nullable String name;
    private @Inspect @Nullable Double damage;
    private @Inspect double velocity;
    private @Inspect ClickAction clickAction;
    private @Inspect Class<? extends Entity> projectile;
    private @Inspect List<PotionEffect> potion;
    private @Inspect Filter destroyFilter;
    private @Inspect Duration coolDown;
    private @Inspect boolean throwable;
    private @Inspect Kit victimKit;
    private @Inspect Kit attackerKit;

    public ProjectileDefinitionImpl(@Nullable String name,
                                    @Nullable Double damage,
                                    double velocity,
                                    ClickAction clickAction,
                                    Class<? extends Entity> entity,
                                    List<PotionEffect> potion,
                                    Filter destroyFilter,
                                    Duration coolDown,
                                    boolean throwable,
                                    Kit victimKit,
                                    Kit attackerKit) {
        this.name = name;
        this.damage = damage;
        this.velocity = velocity;
        this.clickAction = clickAction;
        this.projectile = entity;
        this.potion = potion;
        this.destroyFilter = destroyFilter;
        this.coolDown = coolDown;
        this.throwable = throwable;
        this.victimKit = victimKit;
        this.attackerKit = attackerKit;
    }

    @Override
    public @Nullable String getName() {
        return name;
    }

    @Override
    @Nullable
    public Double damage() {
        return damage;
    }

    @Override
    public double velocity() {
        return velocity;
    }

    @Override
    public ClickAction clickAction() {
        return clickAction;
    }

    @Override
    public Class<? extends Entity> projectile() {
        return projectile;
    }

    @Override
    public List<PotionEffect> potion() {
        return potion;
    }

    @Override
    public Filter destroyFilter() {
        return destroyFilter;
    }

    @Override
    public Duration cooldown() {
        return coolDown;
    }

    @Override
    public boolean throwable() {
        return throwable;
    }

    @Override
    public Kit victimKit() {
        return victimKit;
    }

    @Override
    public Kit attackerKit() {
        return attackerKit;
    }
}
