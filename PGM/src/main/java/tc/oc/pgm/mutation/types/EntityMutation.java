package tc.oc.pgm.mutation.types;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerSpawnEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import tc.oc.commons.core.collection.WeakHashSet;
import tc.oc.pgm.events.MatchPlayerDeathEvent;
import tc.oc.pgm.events.PlayerChangePartyEvent;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Stream;

import static tc.oc.commons.core.util.Optionals.cast;

/**
 * A mutation module that tracks entity spawns.
 */
public class EntityMutation<E extends Entity> extends KitMutation {

    final Class<E> type;
    final Set<E> entities;
    final Map<Instant, Set<E>> entitiesByTime;
    final Map<MatchPlayer, Set<E>> entitiesByPlayer;
    final Map<E, MatchPlayer> playersByEntity;

    public EntityMutation(Match match, Class<E> type, boolean force) {
        super(match, force);
        this.type = type;
        this.entities = new WeakHashSet<>();
        this.entitiesByTime = new WeakHashMap<>();
        this.entitiesByPlayer = new WeakHashMap<>();
        this.playersByEntity = new WeakHashMap<>();
    }

    /**
     * Gets an immutable stream of all registered entities.
     * @return stream of entities.
     */
    public Stream<E> entities() {
        return ImmutableSet.copyOf(entities).stream();
    }

    /**
     * Gets an immutable stream of all registered entities
     * by the time they spawned in ascending order.
     * @return stream of entities.
     */
    public Stream<E> entitiesByTime() {
        return ImmutableMap.copyOf(entitiesByTime)
                           .entrySet()
                           .stream()
                           .sorted(Map.Entry.comparingByKey(Comparator.comparing(Instant::toEpochMilli)))
                           .flatMap(entry -> ImmutableSet.copyOf(entry.getValue()).stream());
    }

    /**
     * Gets an immutable stream of all registered entities
     * that are owned by a player.
     *
     * If the given player is null, this will return entities
     * with no owner.
     * @param player the optional player.
     * @return stream of entities.
     */
    public Stream<E> entitiesByPlayer(@Nullable MatchPlayer player) {
        return ImmutableSet.copyOf(entitiesByPlayer.getOrDefault(player, new HashSet<>())).stream();
    }

    /**
     * Gets the optional owner of an entity.
     * @param entity the entity to find the owner of.
     * @return the optional player.
     */
    public Optional<MatchPlayer> playerByEntity(Entity entity) {
        return Optional.ofNullable(playersByEntity.get(entity));
    }

    /**
     * Are entities with this spawn reason allowed to spawn?
     * @param reason the spawn reason.
     * @return whether this reason is allowed.
     */
    public boolean allowed(CreatureSpawnEvent.SpawnReason reason) {
        switch(reason) {
            case NATURAL:
            case DEFAULT:
            case CHUNK_GEN:
            case JOCKEY:
            case MOUNT:
                return false;
            default:
                return true;
        }
    }

    /**
     * Register a new spawned entity with an optional owner.
     * @param entity the entity to register.
     * @param owner the optional owner.
     * @return the entity.
     */
    public E register(E entity, @Nullable MatchPlayer owner) {
        entities.add(entity);
        final Instant now = match().getInstantNow();
        final Set<E> byTime = entitiesByTime.getOrDefault(now, new WeakHashSet<>());
        byTime.add(entity);
        entitiesByTime.put(now, byTime);
        if(owner != null) {
            final Set<E> byPlayer = entitiesByPlayer.getOrDefault(owner, new WeakHashSet<>());
            byPlayer.add(entity);
            entitiesByPlayer.put(owner, byPlayer);
            playersByEntity.put(entity, owner);
        }
        return entity;
    }

    /**
     * Removes the entity from the world.
     *
     * Typically this should be {@link Entity#remove()},
     * but it can also expire the entity after a couple of seconds.
     * @param entity the entity to remove.
     */
    public void remove(E entity) {
        entity.remove();
    }

    /**
     * Unregister and remove the given entity.
     * @param entity the entity.
     */
    public void despawn(E entity) {
        entities.remove(entity);
        playersByEntity.remove(entity);
        Stream.of(entitiesByTime, entitiesByPlayer)
              .flatMap(map -> ImmutableList.copyOf(map.values()).stream())
              .forEach(set -> set.remove(entity));
        remove(entity);
    }

    /**
     * Unregister and remove any entities owned by the given player.
     * @param player the owner of the entities.
     */
    public void despawn(MatchPlayer player) {
        ImmutableSet.copyOf(entitiesByPlayer.getOrDefault(player, new HashSet<>())).forEach(this::despawn);
    }

    /**
     * Spawn an entity at the given location with no owner.
     * @see #spawn(Location, Class, MatchPlayer)
     * @return the entity.
     */
    public E spawn(Location location, Class<E> entityClass) {
        return spawn(location, entityClass, null);
    }

    /**
     * Spawn an entity at the given location.
     * @param location the location to spawn the entity.
     * @param entityClass the class of the entity.
     * @param owner the optional owner of the entity.
     * @return the entity.
     */
    public E spawn(Location location, Class<E> entityClass, @Nullable MatchPlayer owner) {
        E entity = world().spawn(location, entityClass);
        cast(entity, LivingEntity.class).ifPresent(living -> {
            living.setCanPickupItems(false);
            living.setRemoveWhenFarAway(true);
            EntityEquipment equipment = living.getEquipment();
            equipment.setHelmetDropChance(0);
            equipment.setChestplateDropChance(0);
            equipment.setLeggingsDropChance(0);
            equipment.setBootsDropChance(0);
        });
        return register(entity, owner);
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGHEST)
    public void onPlayerSpawnEntity(PlayerSpawnEntityEvent event) {
        match().participant((Entity) event.getPlayer())
               .ifPresent(player -> cast(event.getEntity(), type)
               .ifPresent(entity -> {
                   register(entity, player);
                   event.setCancelled(false);
               }));
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGHEST)
    public void onEntitySpawn(CreatureSpawnEvent event) {
        boolean allowed = allowed(event.getSpawnReason());
        event.setCancelled(!allowed);
        if(allowed) {
            cast(event.getEntity(), type)
                      .filter(entity -> !playerByEntity(entity).isPresent())
                      .ifPresent(entity -> register(entity, null));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDeath(MatchPlayerDeathEvent event) {
        despawn(event.getVictim());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPartyChange(PlayerChangePartyEvent event) {
        despawn(event.getPlayer());
    }

    @Override
    public void remove(MatchPlayer player) {
        despawn(player);
        super.remove(player);
    }

    @Override
    public void disable() {
        entities().forEach(this::despawn);
        Stream.of(entitiesByTime, entitiesByPlayer, playersByEntity).forEach(Map::clear);
        super.disable();
    }

}
