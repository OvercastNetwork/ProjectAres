package tc.oc.pgm.pickup;

import com.sk89q.minecraft.util.commands.ChatColor;
import org.bukkit.*;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.geometry.Cuboid;
import java.time.Duration;
import java.time.Instant;
import tc.oc.commons.bukkit.geometry.Capsule;
import tc.oc.commons.bukkit.geometry.Sphere;
import tc.oc.pgm.cooldown.CooldownPlayerFacet;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.features.Feature;
import tc.oc.pgm.kits.KitPlayerFacet;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.Repeatable;

import java.util.Optional;

import static tc.oc.commons.core.util.TimeUtils.isEqualOrBeforeNow;
import static tc.oc.commons.core.util.TimeUtils.isInfPositive;
import static tc.oc.commons.core.util.Utils.ifInstance;

@ListenerScope(MatchScope.RUNNING)
public class Pickup implements Feature<PickupDefinition>, Listener {

    private final Match match;
    private final World world;
    private final PickupDefinition data;
    private Optional<Entity> entity;
    private Optional<Instant> spawnAt;

    public Pickup(Match match, PickupDefinition data) {
        this.match = match;
        this.world = match.getWorld();
        this.data = data;
        this.entity = Optional.empty();
        this.spawnAt = Optional.of(match.getInstantNow());
    }

    public boolean isSpawned() {
        return entity.isPresent();
    }

    public void spawn() {
        Location location = data.region().getRandom(match.getRandom()).toLocation(world);
        effects(Sound.BLOCK_NOTE_BASEDRUM, Particle.CLOUD);
        Entity entity = world.spawn(location, (Class) data.appearance().getEntityClass());
        entity.setKnockbackReduction(1);
        data.name().ifPresent(name -> {
            entity.setCustomName(ChatColor.translateAlternateColorCodes('`', name));
            entity.setCustomNameVisible(true);
        });
        switch(data.appearance()) { // TODO: Support more appearances later
            case ENDER_CRYSTAL:
                ifInstance(entity, EnderCrystal.class, crystal -> crystal.setShowingBottom(false)); break;
            case PRIMED_TNT:
                ifInstance(entity, TNTPrimed.class, tnt -> tnt.setFuseTicks(Integer.MAX_VALUE)); break;
        }
        this.entity = Optional.of(entity);
        spawnAt = Optional.empty();
    }

    public void despawn(boolean delay) {
        if(!data.respawn().equals(Duration.ZERO)) {
            entity.ifPresent(entity -> {
                entity.remove();
                effects(Sound.BLOCK_LAVA_POP, Particle.SMOKE_LARGE);
                this.entity = Optional.empty();
            });
            spawnAt = Optional.ofNullable(delay ? isInfPositive(data.respawn()) ? null : match.getInstantNow().plus(data.respawn()) : match.getInstantNow());
        } else {
            entity.ifPresent(entity -> effects(Sound.BLOCK_LAVA_POP, Particle.SLIME));
        }
    }

    private void effects(Sound sound, Particle particle) {
        entity.ifPresent(entity -> {
            if(data.effects()) {
                Cuboid box = entity.getBoundingBox();
                for(int i = 0; i < box.volume(); i++) {
                    world.spawnParticle(particle, box.randomPointInside(match.getRandom()).toLocation(world), 3, 1, 1, 1, 0);
                }
            }
            if(data.sounds()) {
                world.playSound(entity.getLocation(), sound, 1, 1);
            }
        });
    }

    @Repeatable(scope = MatchScope.LOADED)
    public void tick() {
        if(isSpawned()) {
            if(data.visible().query(match).isDenied()) {
                despawn(false);
            }
        } else {
            if(spawnAt.isPresent() && isEqualOrBeforeNow(match.getInstantNow(), spawnAt.get()) && data.visible().query(match).isAllowed()) {
                spawn();
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if(!isSpawned()) return;
        MatchPlayer player = match.getPlayer(event.getPlayer());
        if(player != null) {
            CooldownPlayerFacet cooler = player.facet(CooldownPlayerFacet.class);
            if(player.canInteract() &&
               cooler.isNotCooling(this) &&
               Capsule.fromEndpointsAndRadius(event.getFrom().position(),
                                              event.getTo().position(),
                                              0.5)
                      .intersects(Sphere.fromCircumscribedCuboid(entity.get().getBoundingBox())) &&
               data.pickup().query(player).isAllowed()) {

                cooler.coolFor(this, data.cooldown());
                despawn(true);
                player.facet(KitPlayerFacet.class).applyKit(data.kit(), false);
            }
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if(isSpawned() && event.getEntity().equals(entity.get())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDeath(EntityDeathEvent event) {
        if(isSpawned() && event.getEntity().equals(entity.get())) {
            event.setDroppedExp(0);
            event.getDrops().clear();
            despawn(false);
        }
    }

    @Override
    public PickupDefinition getDefinition() {
        return data;
    }

}
