package tc.oc.pgm.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import tc.oc.commons.bukkit.inventory.InventorySlot;
import tc.oc.commons.core.ListUtils;
import tc.oc.commons.core.collection.InstantMap;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.random.AdvancingEntropy;
import tc.oc.commons.core.random.Entropy;
import tc.oc.commons.core.stream.Collectors;
import tc.oc.commons.core.util.Pair;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.FilterDispatcher;
import tc.oc.pgm.filters.query.EntityQuery;
import tc.oc.pgm.filters.query.ITransientQuery;
import tc.oc.pgm.filters.query.TransientPlayerQuery;
import tc.oc.pgm.itemmeta.ItemModifier;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchPlayerFinder;
import tc.oc.pgm.time.WorldTickClock;

public class FillListener implements Listener {

    private final Logger logger;
    private final World world;
    private final MatchPlayerFinder playerFinder;
    private final ItemModifier itemModifier;
    private final List<Filler> fillers;
    private final List<Cache> caches;

    private final InstantMap<Pair<Fillable, Filler>> filledAt;

    @Inject private FillListener(Loggers loggers, World world, WorldTickClock clock, MatchPlayerFinder playerFinder, ItemModifier itemModifier, FilterDispatcher filterDispatcher, List<Filler> fillers, List<Cache> caches) {
        this.logger = loggers.get(getClass());
        this.fillers = fillers;
        this.playerFinder = playerFinder;
        this.world = world;
        this.caches = caches;
        this.itemModifier = itemModifier;
        this.filledAt = new InstantMap<>(clock);

        fillers.forEach(filler -> {
            filler.refill_trigger().ifPresent(trigger -> {
                filterDispatcher.onRise(Match.class, trigger, match -> {
                    filledAt.keySet().removeIf(fill -> filler.equals(fill.second));
                });
            });
        });
    }

    private static boolean isFillable(BlockState block) {
        return block instanceof InventoryHolder;
    }

    private static boolean isFillable(Entity entity) {
        return entity instanceof InventoryHolder && !(entity instanceof Player);
    }

    /**
     * Return a predicate that applies a Filter to the given InventoryHolder,
     * or null if the InventoryHolder is not something that we should be filling.
     */
    private static @Nullable Predicate<Filter> passesFilter(InventoryHolder holder) {
        if(holder instanceof DoubleChest) {
            final DoubleChest doubleChest = (DoubleChest) holder;
            return filter -> !filter.denies((Chest) doubleChest.getLeftSide()) ||
                             !filter.denies((Chest) doubleChest.getRightSide());
        } else if(holder instanceof BlockState) {
            return filter -> !filter.denies((BlockState) holder);
        } else if(holder instanceof Player) {
            // This happens with crafting inventories, and possibly other transient inventory types
            // Pretty sure we never want to fill an inventory held by the player
            return null;
        } else if(holder instanceof Entity) {
            return filter -> !filter.denies(new EntityQuery((Entity) holder));
        } else {
            // If we're not sure what it is, don't fill it
            return null;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        final MatchPlayer opener = playerFinder.getParticipant(event.getActor());
        if(opener == null) return;

        final Inventory inventory = event.getInventory();
        final Predicate<Filter> passesFilter = passesFilter(inventory.getHolder());
        if(passesFilter == null) return;

        logger.fine(() -> opener.getName() + " opened a " + inventory.getHolder().getClass().getSimpleName());

        // Find all Fillers that apply to the holder of the opened inventory
        final List<Filler> fillers = this.fillers.stream()
                                                 .filter(filler -> passesFilter.test(filler.filter()))
                                                 .collect(Collectors.toImmutableList());
        if(fillers.isEmpty()) return;

        logger.fine(() -> "Found fillers " + fillers.stream()
                                                    .map(Filler::identify)
                                                    .collect(java.util.stream.Collectors.joining(", ")));

        // Find all Caches that the opened inventory is part of
        final List<Fillable> fillables = new ArrayList<>();
        for(Cache cache : caches) {
            if(passesFilter.test(cache.region()) && passesFilter.test(cache.filter())) {
                fillables.add(new FillableCache(cache));
            }
        }
        // If the inventory is not in any Cache, just fill it directly
        if(fillables.isEmpty()) {
            fillables.add(new FillableInventory(inventory));
        }

        fillables.forEach(fillable -> fillable.fill(opener, fillers));
    }

    private abstract class Fillable {

        abstract Stream<Inventory> inventories();

        void fill(MatchPlayer opener, List<Filler> fillers) {
            // Build a short list of Fillers that are NOT cooling down from a previous fill
            final List<Filler> coolFillers = ListUtils.filteredCopyOf(fillers, (Filler filler) ->
                null == filledAt.putUnlessNewer(Pair.of(this, filler), filler.refill_interval())
            );

            // Find all the Inventories for this Fillable, and build a map of Fillers to the subset
            // of Inventories that they are allowed to fill, based on the filter of each Filler.
            // Note how duplicate inventories are skipped.
            final SetMultimap<Filler, Inventory> fillerInventories = HashMultimap.create();
            inventories().distinct().forEach(inventory -> {
                final Predicate<Filter> passes = passesFilter(inventory.getHolder());
                for(Filler filler : coolFillers) {
                    if(passes.test(filler.filter())) {
                        fillerInventories.put(filler, inventory);
                    }
                }
            });

            // Do all clearing before we start filling anything
            fillerInventories.asMap().forEach((filler, inventories) -> {
                if(filler.refill_clear()) {
                    inventories().forEach(Inventory::clear);
                }
            });

            // Some things we will need to generate the loot
            final ITransientQuery query = new TransientPlayerQuery(opener);
            final Entropy entropy = new AdvancingEntropy(query.entropy().randomLong());

            fillerInventories.asMap().forEach((filler, inventories) -> {
                // For each Fillter, build a mutable list of slots that it can fill
                final List<InventorySlot> slots = new ArrayList<>();
                inventories.forEach(inv -> {
                    for(int index = 0; index < inv.getSize(); index++) {
                        if(inv.getItem(index) == null) {
                            slots.add(InventorySlot.fromInventoryIndex(inv, index));
                        }
                    }
                });

                if(!slots.isEmpty()) {
                    // Generate the loot for this Filler
                    filler.loot().items().elements(query).forEachOrdered(item -> {
                        if(!slots.isEmpty()) {
                            // For each item, remove a random slot from those remaining,
                            // apply item mods, and put it in the slot.
                            entropy.removeRandomElement(slots)
                                   .putItem(itemModifier.modifyCopy(item));
                        }
                    });
                }
            });
        }
    }

    private class FillableInventory extends Fillable {
        final Inventory inventory;

        FillableInventory(Inventory inventory) {
            this.inventory = inventory;
        }

        @Override
        public int hashCode() {
            return inventory.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof FillableInventory &&
                   inventory.equals(((FillableInventory) obj).inventory);
        }

        @Override
        Stream<Inventory> inventories() {
            return Stream.of(inventory);
        }
    }

    private class FillableCache extends Fillable {
        final Cache cache;

        private FillableCache(Cache cache) {
            this.cache = cache;
        }

        @Override
        public int hashCode() {
            return cache.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof FillableCache &&
                   cache.equals(((FillableCache) obj).cache);
        }

        @Override
        Stream<Inventory> inventories() {
            return Stream.concat(
                cache.region()
                     .tileEntities(world)
                     .filter(FillListener::isFillable)
                     .filter(block -> !cache.filter().denies(block))
                     .map(block -> ((InventoryHolder) block).getInventory()),
                cache.region()
                     .entities(world)
                     .filter(FillListener::isFillable)
                     .filter(entity -> !cache.filter().denies(new EntityQuery(entity)))
                     .map(entity -> ((InventoryHolder) entity).getInventory())
            );
        }
    }
}
