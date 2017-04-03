package tc.oc.pgm.mutation.types.targetable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import org.apache.commons.lang.math.Fraction;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Slime;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import tc.oc.commons.core.random.ImmutableWeightedRandomChooser;
import tc.oc.commons.core.random.WeightedRandomChooser;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.Repeatable;
import tc.oc.pgm.mutation.types.EntityMutation;
import tc.oc.pgm.mutation.types.kit.EnchantmentMutation;
import tc.oc.pgm.mutation.types.TargetMutation;
import tc.oc.pgm.points.PointProviderAttributes;
import tc.oc.pgm.points.RandomPointProvider;
import tc.oc.pgm.points.RegionPointProvider;
import tc.oc.pgm.regions.CuboidRegion;

import javax.annotation.Nullable;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static tc.oc.commons.core.random.RandomUtils.nextBoolean;

public class ApocalypseMutation extends EntityMutation<LivingEntity> implements TargetMutation {

    final static ImmutableMap<Integer, Integer> AMOUNT_MAP = new ImmutableMap.Builder<Integer, Integer>()
            .put(3,  25)
            .put(5,  20)
            .put(10, 15)
            .put(20, 5)
            .put(50, 1)
            .build();

    final static ImmutableMap<Integer, Integer> STACK_MAP = new ImmutableMap.Builder<Integer, Integer>()
            .put(1, 100)
            .put(2, 25)
            .build();

    final static ImmutableMap<EntityType, Integer> AERIAL_MAP = new ImmutableMap.Builder<EntityType, Integer>()
            .put(EntityType.VEX,   5)
            .put(EntityType.BLAZE, 1)
            .build();

    final static ImmutableMap<EntityType, Integer> GROUND_MAP = new ImmutableMap.Builder<EntityType, Integer>()
            .put(EntityType.SPIDER,      50)
            .put(EntityType.ZOMBIE,      40)
            .put(EntityType.CREEPER,     30)
            .put(EntityType.HUSK,        20)
            .put(EntityType.CAVE_SPIDER, 10)
            .put(EntityType.PIG_ZOMBIE,  1)
            .build();

    final static ImmutableMap<EntityType, Integer> RANGED_MAP = new ImmutableMap.Builder<EntityType, Integer>()
            .put(EntityType.SKELETON,        50)
            .put(EntityType.STRAY,           20)
            .put(EntityType.BLAZE,           20)
            .put(EntityType.GHAST,           10)
            .put(EntityType.SHULKER,         5)
            .put(EntityType.WITCH,           5)
            .put(EntityType.WITHER_SKELETON, 1)
            .build();

    final static ImmutableMap<EntityType, Integer> FLYABLE_MAP = new ImmutableMap.Builder<EntityType, Integer>()
            .putAll(AERIAL_MAP)
            .put(EntityType.BAT, 10)
            .build();

    final static ImmutableMap<EntityType, Integer> PASSENGER_MAP = new ImmutableMap.Builder<EntityType, Integer>()
            .putAll(RANGED_MAP)
            .put(EntityType.CREEPER,   40)
            .put(EntityType.PRIMED_TNT, 1)
            .build();

    final static ImmutableMap<EntityType, Integer> CUBE_MAP = new ImmutableMap.Builder<EntityType, Integer>()
            .put(EntityType.SLIME,       10)
            .put(EntityType.MAGMA_CUBE,  1)
            .build();

    final static WeightedRandomChooser<Integer, Integer> AMOUNT = new ImmutableWeightedRandomChooser<>(AMOUNT_MAP);
    final static WeightedRandomChooser<Integer, Integer> STACK = new ImmutableWeightedRandomChooser<>(STACK_MAP);
    final static WeightedRandomChooser<EntityType, Integer> AERIAL = new ImmutableWeightedRandomChooser<>(AERIAL_MAP);
    final static WeightedRandomChooser<EntityType, Integer> GROUND = new ImmutableWeightedRandomChooser<>(GROUND_MAP);
    final static WeightedRandomChooser<EntityType, Integer> RANGED = new ImmutableWeightedRandomChooser<>(RANGED_MAP);
    final static WeightedRandomChooser<EntityType, Integer> FLYABLE = new ImmutableWeightedRandomChooser<>(FLYABLE_MAP);
    final static WeightedRandomChooser<EntityType, Integer> PASSENGER = new ImmutableWeightedRandomChooser<>(PASSENGER_MAP);
    final static WeightedRandomChooser<EntityType, Integer> CUBE = new ImmutableWeightedRandomChooser<>(CUBE_MAP);

    final static Range<Integer> FREQUENCY = Range.closed(5, 30); // Seconds between entity spawns
    final static int DISTANCE = 15; // Max distance entities spawn from players
    final static int PARTICIPANT_ENTITIES = 25; // Max entities on the field per participant
    final static int MAX_ENTITIES = 500; // Max total entities on the field
    final static Range<Integer> AIR_OFFSET = Range.closed(DISTANCE / 4, DISTANCE); // Y-axis offset for spawning flying entities
    final static Fraction SPECIAL_CHANCE = Fraction.ONE_FIFTH; // Chance of a special attribute occuring in an entity
    final static int SPECIAL_MULTIPLIER = 3; // Multiplier for special attributes

    long time; // world time
    Instant next; // next time to spawn entities
    final PointProviderAttributes attributes; // attributes to choosing random points

    public ApocalypseMutation(Match match) {
        super(match, false);
        this.attributes = new PointProviderAttributes(null, null, true, false);
    }

    /**
     * Get the maximum amount of entities that can be spawned.
     */
    public int entitiesMax() {
        return Math.min((int) match().participants().count() * PARTICIPANT_ENTITIES, MAX_ENTITIES);
    }

    /**
     * Get the number of available slots are left for additional entities to spawn.
     */
    public int entitiesLeft() {
        return entitiesMax() - world().getLivingEntities().size() + (int) match().participants().count();
    }

    /**
     * Generate a random spawn point given two locations.
     */
    public Optional<Location> location(Location start, Location end) {
        return Optional.ofNullable(new RandomPointProvider(Collections.singleton(new RegionPointProvider(new CuboidRegion(start.position(), end.position()), attributes))).getPoint(match(), null));
    }

    /**
     * Spawn a cohort of entities at the given location.
     * @param location location to spawn the entity.
     * @param ground whether the location is on the ground.
     */
    public void spawn(Location location, boolean ground) {
        int slots = entitiesLeft();
        int queued = AMOUNT.choose(entropy());
        // Remove any entities that may be over the max limit
        despawn(queued - slots);
        // Determine whether the entities should be airborn
        int stack = STACK.choose(entropy());
        boolean air = !ground || nextBoolean(random(), SPECIAL_CHANCE);
        if(air) {
            stack += (stack == 1 && random().nextBoolean() ? 1 : 0);
            location.add(0, entropy().randomInt(AIR_OFFSET), 0);
        }
        // Select the random entity chooser based on ground, air, and stacked
        boolean stacked = stack > 1;
        WeightedRandomChooser<EntityType, Integer> chooser;
        if(air) {
            if(stacked) {
                if(ground) {
                    chooser = nextBoolean(random(), SPECIAL_CHANCE) ? CUBE : FLYABLE;
                } else {
                    chooser = FLYABLE;
                }
            } else {
                chooser = AERIAL;
            }
        } else {
            if(stacked) {
                chooser = GROUND;
            } else {
                chooser = random().nextBoolean() ? GROUND : RANGED;
            }
        }
        // Select the specific entity types for the spawn,
        // all entities will have the same sequence of entity type
        // but may have variations (like armor) between them.
        List<EntityType> types = new ArrayList<>();
        for(int i = 0; i < stack; i++) {
            types.add((i == 0 ? chooser : PASSENGER).choose(entropy()));
        }
        // Spawn the mobs and stack them if required
        for(int i = 0; i < queued; i++) {
            Entity last = null;
            for(EntityType type : types) {
                Entity entity = spawn(location, (Class<LivingEntity>) type.getEntityClass());
                if(last != null) {
                    last.setPassenger(entity);
                }
                last = entity;
            }
        }

    }

    @Override
    public LivingEntity spawn(Location location, Class<LivingEntity> entityClass, @Nullable MatchPlayer owner) {
        LivingEntity entity = super.spawn(location, entityClass, owner);
        EnchantmentMutation enchant = new EnchantmentMutation(match());
        EntityEquipment equipment = entity.getEquipment();
        entity.setVelocity(Vector.getRandom());
        ItemStack held = null;
        switch(entity.getType()) {
            case SKELETON:
            case WITHER_SKELETON:
            case STRAY:
                held = item(Material.BOW);
                break;
            case ZOMBIE:
            case ZOMBIE_VILLAGER:
            case HUSK:
                Zombie zombie = (Zombie) entity;
                zombie.setBaby(nextBoolean(random(), SPECIAL_CHANCE));
                break;
            case PIG_ZOMBIE:
                PigZombie pigZombie = (PigZombie) entity;
                pigZombie.setAngry(true);
                pigZombie.setAnger(Integer.MAX_VALUE);
                held = item(Material.GOLD_SWORD);
                break;
            case CREEPER:
                Creeper creeper = (Creeper) entity;
                creeper.setPowered(nextBoolean(random(), SPECIAL_CHANCE));
                world().strikeLightningEffect(location);
                break;
            case PRIMED_TNT:
                TNTPrimed tnt = (TNTPrimed) entity;
                tnt.setFuseTicks(tnt.getFuseTicks() * SPECIAL_MULTIPLIER);
                break;
            case SLIME:
            case MAGMA_CUBE:
                Slime slime = (Slime) entity;
                slime.setSize(slime.getSize() * SPECIAL_MULTIPLIER);
                break;
            case SKELETON_HORSE:
                world().strikeLightning(location);
                break;
        }
        if(held != null && random().nextBoolean()) {
            enchant.apply(held, equipment);
            equipment.setItemInMainHand(held);
        }
        return entity;
    }

    /**
     * Select the entities that have lived the longest and remove them
     * to make room for new entities.
     * @param amount the amount of entities to despawn.
     */
    public void despawn(long amount) {
        entitiesByTime().limit(Math.max(0, amount)).forEachOrdered(this::despawn);
    }

    @Override
    public void target(List<MatchPlayer> players) {
        // At least one player is required to spawn mobs
        if(players.size() >= 1) {
            Location start, end;
            start = players.get(0).getLocation(); // player 1 is the first location
            if(players.size() >= 2) {
                end = players.get(1).getLocation(); // if player 2 exists, they are the second location
            } else { // if no player 2, generate a random location near player 1
                end = start.clone().add(Vector.getRandom().multiply(DISTANCE));
            }
            Optional<Location> location = location(start, end);
            if(location.isPresent()) { // if the location is safe (on ground)
                spawn(location.get(), true);
            } else { // if the location was not safe, generate a simple midpoint location
                spawn(start.position().midpoint(end.position()).toLocation(world()), false);
            }
        }
    }

    @Override
    public int targets() {
        return 2; // Always require 2 targets to generate a spawn location between them
    }

    @Override
    public Instant next() {
        return next;
    }

    @Override
    public void next(Instant time) {
        next = time;
    }

    @Override
    public Duration frequency() {
        return Duration.ofSeconds(entropy().randomInt(FREQUENCY));
    }

    @Override
    public void enable() {
        super.enable();
        TargetMutation.super.enable();
        time = world().getTime();
    }

    @Repeatable
    public void tick() {
        TargetMutation.super.tick();
        world().setTime(16000); // Night time to prevent flaming entities
    }

    @Override
    public void disable() {
        world().setTime(time);
        despawn(entities().count());
        super.disable();
    }

}
