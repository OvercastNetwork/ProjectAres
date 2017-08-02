package tc.oc.pgm.mutation.types.kit;

import com.google.common.collect.ImmutableMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import tc.oc.commons.core.random.ImmutableWeightedRandomChooser;
import tc.oc.commons.core.random.WeightedRandomChooser;
import tc.oc.pgm.kits.FreeItemKit;
import tc.oc.pgm.kits.Kit;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.mutation.types.EntityMutation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static tc.oc.commons.core.util.Optionals.cast;

public class EquestrianMutation extends EntityMutation<AbstractHorse> {

    final static ImmutableMap<EntityType, Integer> TYPE_MAP = new ImmutableMap.Builder<EntityType, Integer>()
            .put(EntityType.HORSE,          100)
            // FIXME: Saddles do not work on these horses
            //.put(EntityType.SKELETON_HORSE, 5)
            //.put(EntityType.ZOMBIE_HORSE,   5)
            //.put(EntityType.LLAMA,          1)
            .build();

    final static ImmutableMap<Material, Integer> ARMOR_MAP = new ImmutableMap.Builder<Material, Integer>()
            .put(Material.SADDLE,          25)
            .put(Material.GOLD_BARDING,    10)
            .put(Material.IRON_BARDING,    5)
            .put(Material.DIAMOND_BARDING, 1)
            .build();

    final static WeightedRandomChooser<EntityType, Integer> TYPES = new ImmutableWeightedRandomChooser<>(TYPE_MAP);
    final static WeightedRandomChooser<Material, Integer> ARMOR = new ImmutableWeightedRandomChooser<>(ARMOR_MAP);

    public EquestrianMutation(Match match) {
        super(match, AbstractHorse.class, false);
    }

    @Override
    public void enable() {
        super.enable();
        this.itemRemove.add(Material.LEATHER);
        this.itemRemove.addAll(ARMOR_MAP.keySet());
    }

    @Override
    public void kits(MatchPlayer player, List<Kit> kits) {
        super.kits(player, kits);
        Location location = player.getLocation();
        // If there is not enough room to spawn a horse, give the player
        // an egg so they can spawn it later
        if(!spawnable(location)) {
            ItemStack item = item(Material.MONSTER_EGG);
            SpawnEggMeta egg = (SpawnEggMeta) item.getItemMeta();
            egg.setSpawnedType(TYPES.choose(entropy()));
            item.setItemMeta(egg);
            kits.add(new FreeItemKit(item));
        }
    }

    @Override
    public void apply(MatchPlayer player) {
        super.apply(player);
        Location location = player.getLocation();
        if(spawnable(location)) {
            spawn(location, (Class<AbstractHorse>) TYPES.choose(entropy()).getEntityClass(), player);
        }
    }

    @Override
    public AbstractHorse register(AbstractHorse horse, @Nullable MatchPlayer owner) {
        super.register(horse, owner);
        if(owner != null) {
            horse.setAdult();
            horse.setJumpStrength(2 * entropy().randomDouble());
            horse.setDomestication(1);
            horse.setMaxDomestication(1);
            horse.setTamed(true);
            horse.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0));
            horse.setOwner(owner.getBukkit());
            horse.setPassenger(owner.getBukkit());
            cast(horse, Horse.class).ifPresent(horsey -> {
                horsey.setStyle(entropy().randomElement(Horse.Style.values()));
                horsey.setColor(entropy().randomElement(Horse.Color.values()));
                HorseInventory inventory = horsey.getInventory();
                inventory.setSaddle(item(Material.SADDLE));
                inventory.setArmor(item(ARMOR.choose(entropy())));
            });
        }
        return horse;
    }

    public boolean spawnable(Location location) {
        // Allow at least 4 blocks of air from the feet of the player
        // to allow a horse to be spawned
        for(int i = 0; i <= 4; i++) {
            if(!location.clone().add(0, i, 0).getBlock().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if(playerByEntity(event.getEntity()).flatMap(MatchPlayer::competitor)
                .equals(match().participant(event.getDamager()).flatMap(MatchPlayer::competitor))) {
            event.setCancelled(true);
        }
    }

}