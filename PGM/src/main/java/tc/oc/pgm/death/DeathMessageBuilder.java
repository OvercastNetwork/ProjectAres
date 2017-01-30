package tc.oc.pgm.death;

import java.util.NavigableSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.Components;
import tc.oc.commons.bukkit.chat.NameStyle;
import tc.oc.commons.bukkit.localization.Translations;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.ParticipantState;
import tc.oc.pgm.tracker.damage.BlockInfo;
import tc.oc.pgm.tracker.damage.DamageInfo;
import tc.oc.pgm.tracker.damage.EntityInfo;
import tc.oc.pgm.tracker.damage.ExplosionInfo;
import tc.oc.pgm.tracker.damage.FallInfo;
import tc.oc.pgm.tracker.damage.FallingBlockInfo;
import tc.oc.pgm.tracker.damage.FireInfo;
import tc.oc.pgm.tracker.damage.GenericDamageInfo;
import tc.oc.pgm.tracker.damage.ItemInfo;
import tc.oc.pgm.tracker.damage.MeleeInfo;
import tc.oc.pgm.tracker.damage.MobInfo;
import tc.oc.pgm.tracker.damage.PhysicalInfo;
import tc.oc.pgm.tracker.damage.PotionInfo;
import tc.oc.pgm.tracker.damage.ProjectileInfo;
import tc.oc.pgm.tracker.damage.RangedInfo;
import tc.oc.pgm.tracker.damage.SpleefInfo;
import tc.oc.pgm.tracker.damage.TrackerInfo;

public class DeathMessageBuilder {

    static final Set<String> SPLAT_KEYS = ImmutableSet.of("death.fall.ground", "death.fall.ground.distance");
    static final Set<UUID> SPLAT_PLAYERS = ImmutableSet.of(
        UUID.fromString("6f21f5e3-544b-48a9-9670-63aec36038c4") // HIVE_Raven
    );

    static class NoMessage extends Exception {}

    static NavigableSet<String> allKeys;

    static NavigableSet<String> getAllKeys() {
        if(allKeys == null) {
            allKeys = Translations.get().getKeys("death.");
        }
        return allKeys;
    }

    private static final long SNIPE_DISTANCE = 60;
    private static final int TRIPPED_HEIGHT = 5;
    private static final int NOTABLE_HEIGHT = 12;
    private static final int ORBIT_HEIGHT = 60;

    private final Logger logger;
    private final MatchPlayer victim;
    private final @Nullable ParticipantState killer;

    private String key;
    private BaseComponent weapon = Components.blank();
    private BaseComponent mob = Components.blank();
    private Long distance;

    public DeathMessageBuilder(MatchPlayer victim, DamageInfo damageInfo, Logger logger) {
        this.victim = victim;
        this.killer = damageInfo.getAttacker();
        this.logger = logger;

        build(damageInfo);
    }

    public BaseComponent getMessage() {
        return new TranslatableComponent(key, getArgs());
    }

    BaseComponent[] getArgs() {
        BaseComponent[] args = new BaseComponent[5];
        args[0] = victim.getStyledName(NameStyle.COLOR);
        args[1] = killer == null ? Components.blank() : killer.getStyledName(NameStyle.COLOR);
        args[2] = weapon;
        args[3] = mob;
        args[4] = distance == null ? Components.blank() : new Component(String.valueOf(distance));
        return args;
    }

    void setDistance(double n) {
        if(!Double.isNaN(n)) {
            distance = Math.round(Math.max(0, n));
            if(distance == 1l) distance = 2l; // Cleverly ensure the text is always plural
        }
    }

    /*
     * Primitive methods for manipulating the key
     */

    /**
     * Test if the given string is a prefix of any existing key
     */
    boolean exists(String prefix) {
        String key = getAllKeys().ceiling(prefix);
        return key != null && key.startsWith(prefix);
    }

    /**
     * Return a new key built from the current key with the given tokens appended
     */
    String append(String... tokens) {
        String newKey = key;
        for(String token : tokens) {
            newKey += '.' + token;
        }
        return newKey;
    }

    /**
     * Try to append an optional sequence of tokens to the current key.
     * If the new key is invalid, the current key is not changed.
     */
    boolean option(String... tokens) {
        String newKey = append(tokens);
        if(exists(newKey)) {
            key = newKey;
            return true;
        }
        return false;
    }

    /**
     * Append a sequence of tokens to the current key.
     * @throws NoMessage if the new key is not valid
     */
    void require(String... tokens) throws NoMessage {
        String newKey = append(tokens);
        if(!exists(newKey)) {
            logger.warning("Generated invalid death message key: " + newKey);
            throw new NoMessage();
        }
        key = newKey;
    }

    /**
     * Assert that the current key is complete and valid.
     * @throws NoMessage if it's not
     */
    void finish() throws NoMessage {
        if(!getAllKeys().contains(key)) {
            throw new NoMessage();
        }
    }

    /*
     * Optional components
     *
     * These methods all try
     * to append something to the key, and return true if successful.
     * If they fail, they leave the key unchanged.
     */

    boolean variant() {
        int count = 0;
        for(; getAllKeys().contains(key + "." + count); count++);

        if(count == 0) return false;

        int variant;
        if(SPLAT_KEYS.contains(key) && count > 1) {
            // Variant 0 of fall message is reserved for special friends
            if(SPLAT_PLAYERS.contains(victim.getBukkit().getUniqueId())) {
                variant = 0;
            } else {
                variant = 1 + victim.getMatch().getRandom().nextInt(count - 1);
            }
        } else {
            variant = victim.getMatch().getRandom().nextInt(count);
        }

        key += "." + variant;
        return true;
    }

    boolean ranged(RangedInfo rangedInfo, @Nullable Location distanceReference) {
        double distance = rangedInfo.distanceFrom(distanceReference);
        if(!Double.isNaN(distance) && option("distance")) {
            setDistance(distance);
            if(distance >= SNIPE_DISTANCE) {
                option("snipe");
            }
            return true;
        }
        return false;
    }

    boolean potion(PotionInfo potionInfo) {
        if(option("potion")) {
            weapon = potionInfo.getLocalizedName();
            return true;
        }
        return false;
    }

    boolean item(ItemInfo itemInfo) {
        if(itemInfo.getItem().getType() != Material.AIR && option("item")) {
            weapon = itemInfo.getLocalizedName();
            return true;
        }
        return false;
    }

    boolean block(BlockInfo blockInfo) {
        if(option("block")) {
            weapon = blockInfo.getLocalizedName();
            return true;
        }
        return false;
    }

    boolean entity(EntityInfo entityInfo) {
        if(option("entity")) {
            weapon = entityInfo.getLocalizedName();
            option(entityInfo.getIdentifier());
            return true;
        }
        return false;
    }

    boolean insentient(@Nullable PhysicalInfo info) {
        if(info instanceof PotionInfo) {
            if(potion((PotionInfo) info)) {
                return true;
            } else if(option("entity")) {
                // PotionInfo.getLocalizedName returns a potion name,
                // which doesn't work outside a potion death message.
                weapon = new TranslatableComponent("item.potion.name");
                return true;
            }
        } else if(info instanceof EntityInfo) {
            return !(info instanceof MobInfo) && entity((EntityInfo) info);
        } else if(info instanceof BlockInfo) {
            return block((BlockInfo) info);
        } else if(info instanceof ItemInfo) {
            return item((ItemInfo) info);
        }

        return false;
    }

    boolean mob(MobInfo mobInfo) {
        if(option("mob")) {
            mob = mobInfo.getLocalizedName();
            option(mobInfo.getIdentifier());
            return true;
        }
        return false;
    }

    boolean physical(@Nullable PhysicalInfo info) {
        if(info instanceof MobInfo) {
            return mob((MobInfo) info);
        } else {
            return insentient(info);
        }
    }

    /*
     * Required components
     *
     * Each of these methods appends several keys to the death message,
     * and generally expects to complete successfully. If they fail, they
     * throw a {@link NoMessage} exception and leave the key in an
     * unknown state.
     */

    void player() throws NoMessage {
        if(killer != null) {
            require("player");
        }
    }

    void attack(@Nullable PhysicalInfo attacker, @Nullable PhysicalInfo weapon) throws NoMessage {
        player();
        if(attacker instanceof MobInfo && !mob((MobInfo) attacker)) {
            return;
        }
        insentient(weapon);
    }

    void generic(GenericDamageInfo info) throws NoMessage {
        switch(info.getDamageType()) {
            case CONTACT: require("cactus"); break;
            case DROWNING: require("drown"); break;
            case LIGHTNING: require("lightning"); break;
            case STARVATION: require("starve"); break;
            case SUFFOCATION: require("suffocate"); break;
            case CUSTOM: require("generic"); break;
        }
        // If we don't know the cause, but we already have a full message (e.g. for a fall),
        // just use what we have. Otherwise, use the "unknown" message.
        if(exists(key)) return;
        require("unknown");
    }

    void melee(MeleeInfo melee) throws NoMessage {
        require("melee");
        attack(melee, melee.getWeapon());
    }

    void magic(PotionInfo potion, @Nullable PhysicalInfo attacker) throws NoMessage {
        require("magic");
        attack(attacker, potion);
    }

    void projectile(ProjectileInfo projectile, Location distanceReference) throws NoMessage {
        if(projectile.getProjectile() instanceof PotionInfo) {
            try {
                magic((PotionInfo) projectile.getProjectile(), projectile.getShooter());
                return;
            } catch(NoMessage ignored) {
                // If we can't generate a magic message (probably because it's part
                // of a fall message), fall back to a projectile message.
            }
        }

        require("projectile");

        if(projectile.getProjectile() instanceof EntityInfo && ((EntityInfo) projectile.getProjectile()).getEntityType() == EntityType.ARROW) {
            // "shot by arrow" is redundant
            attack(projectile.getShooter(), null);
        } else {
            attack(projectile.getShooter(), projectile.getProjectile());
            weapon = projectile.getLocalizedName(); // Projectile name may be different than entity name e.g. custom projectile
        }

        ranged(projectile, distanceReference);
    }

    void squash(FallingBlockInfo fallingBlock) throws NoMessage {
        require("squash");
        attack(null, fallingBlock);
    }

    void explosion(ExplosionInfo explosion, Location distanceReference) throws NoMessage {
        require("explosive");
        player();
        physical(explosion.getExplosive());
        ranged(explosion, distanceReference);
    }

    void fire(FireInfo fire) throws NoMessage {
        require("fire");
        player();
        if(!(fire.getIgniter() instanceof BlockInfo && ((BlockInfo) fire.getIgniter()).getMaterial().getItemType() == Material.FIRE)) {
            // "burned by fire" is redundant
            physical(fire.getIgniter());
        }
    }

    void fall(FallInfo fall) throws NoMessage {
        require("fall");
        require(fall.getTo().name().toLowerCase());

        TrackerInfo cause = fall.getCause();
        if(cause instanceof SpleefInfo) {
            require("spleef");
            DamageInfo breaker = ((SpleefInfo) cause).getBreaker();
            if(breaker instanceof ExplosionInfo) {
                explosion((ExplosionInfo) breaker, fall.getOrigin());
            } else {
                player();
            }
        } else if(cause instanceof DamageInfo) {
            damage((DamageInfo) cause, fall.getOrigin());
        } else if(fall.getTo() == FallInfo.To.GROUND) {
            setDistance(fall.distanceFrom(victim.getBukkit().getLocation()));

            if(distance != null) {
                if(distance <= TRIPPED_HEIGHT) {
                    // Very short falls get a "tripped" message
                    option("tripped");
                } else if(distance >= ORBIT_HEIGHT) {
                    // Very long falls get an "orbit" message
                    option("orbit");
                } else if(victim.getMatch().getRandom().nextFloat() < 0.01f) {
                    // Occasionally they get a rare message
                    option("rare");
                }

                // Show distance if it's high enough and the message supports it
                if(distance >= NOTABLE_HEIGHT) option("distance");
            }
        }
    }

    void damage(DamageInfo info, Location distanceReference) throws NoMessage {
        if(info instanceof MeleeInfo) {
            melee((MeleeInfo) info);
        } else if(info instanceof ProjectileInfo) {
            projectile((ProjectileInfo) info, distanceReference);
        } else if(info instanceof ExplosionInfo) {
            explosion((ExplosionInfo) info, distanceReference);
        } else if(info instanceof FireInfo) {
            fire((FireInfo) info);
        } else if(info instanceof PotionInfo) {
            magic((PotionInfo) info, null);
        } else if(info instanceof FallingBlockInfo) {
            squash((FallingBlockInfo) info);
        } else if(info instanceof FallInfo) {
            fall((FallInfo) info);
        } else if(info instanceof GenericDamageInfo) {
            generic((GenericDamageInfo) info);
        } else {
            throw new NoMessage();
        }
    }

    void build(DamageInfo damageInfo) {
        logger.fine("Generating death message for " + damageInfo);

        try {
            key = "death";
            damage(damageInfo, victim.getBukkit().getLocation());
            variant();
            finish();
        } catch(NoMessage ex) {
            logger.log(Level.SEVERE,
                       "Generated invalid death message '" + key +
                       "' for victim=" + victim +
                       " info=" + damageInfo +
                       " killer=" + killer +
                       " weapon=" + weapon +
                       " mob=" + mob +
                       " distance=" + distance,
                       ex);
            key = "death.generic";
        }
    }
}
