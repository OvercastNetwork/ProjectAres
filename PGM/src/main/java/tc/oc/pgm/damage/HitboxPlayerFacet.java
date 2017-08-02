package tc.oc.pgm.damage;

import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.geometry.Cuboid;
import org.bukkit.geometry.Ray;
import tc.oc.commons.bukkit.util.NMSHacks;
import tc.oc.pgm.map.MapInfo;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchPlayerFacet;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.Repeatable;

public class HitboxPlayerFacet implements MatchPlayerFacet {

    private static final double FAKE_WIDTH = 0.6;
    private static final double FAKE_HEIGHT = 1.8;
    private static final double PLAYER_WIDTH = 0.6;
    private static final double PLAYER_HEIGHT = 1.8;
    private static final double VIEW_RADIUS = 12;

    private final NMSHacks.FakeEntity[] fakes = new NMSHacks.FakeEntity[4];
    private final Location[] locations = new Location[4];

    private final Match match;
    private final HitboxMatchModule mm;
    private final World world;
    private final MatchPlayer matchPlayer;
    private final Player player;
    private final MapInfo mapInfo;
    private final Set<Player> viewers = new HashSet<>();
    private double width = PLAYER_WIDTH;

    @Inject HitboxPlayerFacet(Match match, World world, MatchPlayer matchPlayer, Player player, MapInfo mapInfo) {
        this.match = match;
        this.mapInfo = mapInfo;
        this.mm = match.needMatchModule(HitboxMatchModule.class);
        this.world = world;
        this.matchPlayer = matchPlayer;
        this.player = player;
    }

    @Override
    public void enable() {
        for(int i = 0; i < fakes.length; i++) {
            fakes[i] = new NMSHacks.FakeZombie(world, true);
            locations[i] = new Location(world, 0, 0, 0);
            mm.facets.put(fakes[i].entityId(), this);
        }
    }

    @Override
    public void disable() {
        for(NMSHacks.FakeEntity fake : fakes) {
            mm.facets.remove(fake.entityId());
        }
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public Cuboid hitbox() {
        if(hasFakeHitbox()) {
            final Location location = player.getLocation();
            final double radius = width / 2;
            return Cuboid.between(location.position().minus(radius, 0, radius),
                                  location.position().plus(radius, PLAYER_HEIGHT, radius));
        } else {
            return player.getBoundingBox();
        }
    }

    public static Cuboid hitbox(Match match, LivingEntity victim) {
        if(victim instanceof Player) {
            final MatchPlayer matchVictim = match.getPlayer(victim);
            if(matchVictim != null) {
                return matchVictim.facet(HitboxPlayerFacet.class).hitbox();
            }
        }
        return victim.getBoundingBox();
    }

    public boolean hasFakeHitbox() {
        return matchPlayer.canInteract() && width > PLAYER_WIDTH;
    }

    public Location meleeHitLocation(LivingEntity attacker, double minDistanceFromAttacker, double offsetFromVictim) {
        return meleeHitLocation(match, player, attacker, minDistanceFromAttacker, offsetFromVictim);
    }

    public static Location meleeHitLocation(Match match, LivingEntity victim, LivingEntity attacker, double minDistanceFromAttacker, double offsetFromVictim) {
        // Find the attacker's line-of-sight distance to the victim
        final Ray ray = attacker.getEyeRay();
        double distance = hitbox(match, victim).intersectionDistance(ray);

        // If attacker is not looking at the victim, take a rough guess at the distance
        if(Double.isNaN(distance)) {
            distance = ray.origin().distance(victim.getEyeLocation().position());
        }

        distance = Math.max(minDistanceFromAttacker, distance + offsetFromVictim);
        return ray.atDistance(distance).toLocation(victim.getWorld());
    }

    void onUse(Player user, boolean attack, EquipmentSlot hand) {
        NMSHacks.useEntity(user, player, attack, hand);
    }

    @Repeatable(scope = MatchScope.RUNNING)
    public void updateViewers() {
        final Location location = player.getLocation();
        updateFakeLocations(location);

        final Set<Player> remove = new HashSet<>(viewers);
        if(hasFakeHitbox()) {
            for(Entity entity : world.getNearbyEntities(location, VIEW_RADIUS, VIEW_RADIUS, VIEW_RADIUS)) {
                if(entity instanceof Player && !entity.equals(player)) {
                    final Player viewer = (Player) entity;
                    if(isAttacker(viewer)) {
                        if(viewers.add(viewer)) {
                            spawnFakes(viewer);
                        } else {
                            remove.remove(viewer);
                            moveFakes(viewer);
                        }
                    }
                }
            }
        }

        for(Player viewer : remove) {
            destroyFakes(viewer);
        }
        viewers.removeAll(remove);
    }

    private boolean isAttacker(Player attacker) {
        final MatchPlayer mp = match.getPlayer(attacker);
        return mp != null &&
               mp.canInteract() &&
               (mapInfo.friendlyFire ||
                !mp.getParty().equals(matchPlayer.getParty()));
    }

    private void updateFakeLocations(Location c) {
        final double y = c.getY() - (FAKE_HEIGHT - PLAYER_HEIGHT) / 2;

        final double h = (width - FAKE_WIDTH) / 2;
        final double px = c.getX() + h;
        final double mx = c.getX() - h;
        final double pz = c.getZ() + h;
        final double mz = c.getZ() - h;

        locations[0].setPosition(mx, y, mz);
        locations[1].setPosition(px, y, mz);
        locations[2].setPosition(mx, y, pz);
        locations[3].setPosition(px, y, pz);
    }

    private void destroyFakes(Player viewer) {
        for(NMSHacks.FakeEntity fake : fakes) {
            fake.destroy(viewer);
        }
    }

    private void spawnFakes(Player viewer) {
        for(int i = 0; i < fakes.length; i++) {
            fakes[i].spawn(viewer, locations[i]);
        }
    }

    private void moveFakes(Player viewer) {
        for(int i = 0; i < fakes.length; i++) {
            fakes[i].teleport(viewer, locations[i]);
        }
    }
}
