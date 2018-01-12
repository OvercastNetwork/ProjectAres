package tc.oc.lobby.bukkit.gizmos.halloween;

import com.google.common.collect.ImmutableList;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import tc.oc.commons.core.util.Optionals;

/**
 * Simplistic implementation of PGM's EntityMutation
 *
 * TODO: refactor methods, possibly categorize types of gizmos
 */
public class HeadlessHorse {
    private Player viewer;
    private AbstractHorse horse;

    protected HeadlessHorse(Player viewer) {
        this.viewer = viewer;
    }

    protected AbstractHorse spawn(Location location, Class<AbstractHorse> horse) {
        AbstractHorse entity = viewer.getWorld().spawn(location, horse);
        this.horse = entity;
        Optionals.cast(horse, Horse.class).ifPresent(living -> {
            living.setRemoveWhenFarAway(true);
            living.setCollidable(false);
            living.setCanPickupItems(false);
            living.setInvulnerable(true);
            living.setAdult();
            EntityEquipment entityEquipment = living.getEquipment();
            entityEquipment.setHelmetDropChance(0);
            entityEquipment.setChestplateDropChance(0);
            entityEquipment.setLeggingsDropChance(0);
            entityEquipment.setBootsDropChance(0);
            entity.setTamed(true);
            living.getInventory().setSaddle(new ItemStack(Material.SADDLE));
            living.setPassengers(ImmutableList.of(viewer));
        });
        return entity;
    }

    public void despawn() {
        horse.ejectAll();
        horse.remove();
    }
}
