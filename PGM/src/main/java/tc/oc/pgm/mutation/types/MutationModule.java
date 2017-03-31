package tc.oc.pgm.mutation.types;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import tc.oc.commons.bukkit.item.ItemBuilder;
import tc.oc.commons.core.random.AdvancingEntropy;
import tc.oc.commons.core.random.Entropy;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.mutation.Mutation;
import tc.oc.pgm.mutation.MutationMatchModule;

import java.util.Random;

/**
 * Bits of immutable code that manage a {@link Mutation}.
 *
 * This should be able to load at any time during the match
 * and not cause any problems. This will allow mutations
 * to be forcefully loaded on the fly without any worries
 * of breaking the match state.
 */
@ListenerScope(MatchScope.RUNNING)
public abstract class MutationModule implements Listener {

    protected final Match match;
    protected final World world;
    protected final Entropy entropy;
    protected final Random random;

    /**
     * Constructed when {@link MutationMatchModule#load()}
     * has been called. This will only be constructed if its
     * subsequent {@link Mutation} is enabled for the match.
     *
     * @param match the match for this module.
     */
    public MutationModule(Match match) {
        this.match = match;
        this.world = match.getWorld();
        this.entropy = new AdvancingEntropy();
        this.random = new Random();
    }

    /**
     * Called when the match starts.
     *
     * However, this should be able to be called at any
     * point before the match ends and still work as expected.
     */
    public void enable() {
        match.registerEventsAndRepeatables(this);
    }

    /**
     * Called when the match ends.
     *
     * However, this should be able to be called at any
     * point during a match and still work as expected.
     */
    public void disable() {
        match.unregisterEvents(this);
        match.unregisterRepeatable(this);
    }

    protected static ItemStack item(Material material, int amount) {
        return new ItemBuilder().material(material).amount(amount).unbreakable(true).shareable(false).get();
    }

    protected static ItemStack item(Material material) {
        return item(material, 1);
    }

    protected <E extends Entity> E spawn(Location location, Class<E> entityClass) {
        E entity = world.spawn(location, entityClass);
        if(entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) entity;
            living.setCanPickupItems(false);
            living.setRemoveWhenFarAway(true);
            EntityEquipment equipment = living.getEquipment();
            equipment.setHelmetDropChance(0);
            equipment.setChestplateDropChance(0);
            equipment.setLeggingsDropChance(0);
            equipment.setBootsDropChance(0);
        }
        return entity;
    }

}
