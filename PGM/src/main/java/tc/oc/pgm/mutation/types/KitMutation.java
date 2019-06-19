package tc.oc.pgm.mutation.types;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import tc.oc.commons.bukkit.inventory.Slot;
import tc.oc.commons.core.util.Optionals;
import tc.oc.pgm.killreward.KillReward;
import tc.oc.pgm.killreward.KillRewardMatchModule;
import tc.oc.pgm.kits.ItemKit;
import tc.oc.pgm.kits.Kit;
import tc.oc.pgm.kits.KitPlayerFacet;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Stream;

/**
 * A mutation module that injects special kits on spawn and/or kill.
 */
public class KitMutation extends MutationModule.Impl {

    protected final List<Kit> kits;
    protected final Map<MatchPlayer, List<Kit>> playerKits;
    protected final Map<MatchPlayer, Map<Slot, ItemStack>> savedSlots;
    protected final Set<Material> itemRemove;
    protected final List<KillReward> rewards;
    protected final boolean force;

    public KitMutation(Match match, boolean force) {
        super(match);
        this.kits = new ArrayList<>();
        this.playerKits = new WeakHashMap<>();
        this.savedSlots = new WeakHashMap<>();
        this.itemRemove = new HashSet<>();
        this.rewards = new ArrayList<>();
        this.force = force;
    }

    public KitMutation(Match match, boolean force, Kit... kits) {
        this(match, force);
        Stream.of(kits).forEachOrdered(this.kits::add);
    }

    /**
     * Generates a list of kits to apply for the player.
     * Called inside {@link #apply(MatchPlayer)}.
     * @param player the player that will receive the kits
     * @param kits a mutable list of kits.
     */
    public void kits(MatchPlayer player, List<Kit> kits) {
        kits.addAll(this.kits);
    }

    /**
     * Apply the kits to the player.
     * @param player the player.
     */
    public void apply(MatchPlayer player) {
        List<Kit> kits = new ArrayList<>();
        kits(player, kits);
        playerKits.put(player, kits);
        kits.forEach(kit -> Optionals.cast(kit, ItemKit.class).ifPresent(items -> itemRemove.add(items.item().getType())));
        saved().forEach(slot -> {
            slot.item(player.getInventory()).ifPresent(item -> {
                Map<Slot, ItemStack> slots = savedSlots.getOrDefault(player, new HashMap<>());
                slots.put(slot, (ItemStack) item);
                savedSlots.put(player, slots);
                slot.putItem(player.getInventory(), null);
            });
        });
        kits.forEach(kit -> player.facet(KitPlayerFacet.class).applyKit(kit, force));
    }

    /**
     * Forcefuly remove kits from the player.
     * @param player the player.
     */
    public void remove(MatchPlayer player) {
        playerKits.getOrDefault(player, new ArrayList<>()).stream().filter(Kit::isRemovable).forEach(kit -> kit.remove(player));
        savedSlots.getOrDefault(player, new HashMap<>()).forEach((Slot slot, ItemStack item) -> slot.putItem(player.getInventory(), item));
    }

    /**
     * Any slots in the player's inventory that should be saved
     * before {@link #apply(MatchPlayer)} and restored after {@link #remove(MatchPlayer)}.
     * @return the saved slots.
     */
    public Stream<? extends Slot> saved() {
        return Stream.empty();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onItemDrop(ItemSpawnEvent event) {
        Item entity = event.getEntity();
        if(entity != null) {
            ItemStack item = entity.getItemStack();
            if(item != null && itemRemove.contains(item.getType())) {
                entity.remove();
            }
        }
    }

    @Override
    public void enable() {
        super.enable();
        match().module(KillRewardMatchModule.class).get().rewards().addAll(rewards);
        if(match().hasStarted()) {
            match().participants().forEach(this::apply);
        }
    }

    @Override
    public void disable() {
        match().module(KillRewardMatchModule.class).get().rewards().removeAll(rewards);
        match().participants().forEach(this::remove);
        kits.clear();
        playerKits.clear();
        savedSlots.clear();
        itemRemove.clear();
        rewards.clear();
        super.disable();
    }

}
