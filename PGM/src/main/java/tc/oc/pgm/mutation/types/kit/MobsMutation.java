package tc.oc.pgm.mutation.types.kit;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SpawnEggMeta;
import tc.oc.commons.core.random.ImmutableWeightedRandomChooser;
import tc.oc.commons.core.random.WeightedRandomChooser;
import tc.oc.pgm.kits.FreeItemKit;
import tc.oc.pgm.kits.Kit;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.mutation.types.EntityMutation;

import java.util.List;

public class MobsMutation extends EntityMutation<LivingEntity> {

    final static ImmutableMap<EntityType, Integer> TYPE_MAP = new ImmutableMap.Builder<EntityType, Integer>()
            .put(EntityType.ZOMBIE,          50)
            .put(EntityType.SKELETON,        40)
            .put(EntityType.SPIDER,          40)
            .put(EntityType.CREEPER,         30)
            .put(EntityType.BLAZE,           20)
            .put(EntityType.GHAST,           20)
            .put(EntityType.SHULKER,         20)
            .put(EntityType.WITCH,           10)
            .put(EntityType.ENDERMAN,        10)
            .put(EntityType.PIG_ZOMBIE,      5)
            .put(EntityType.WITHER_SKELETON, 1)
            .build();

    final static WeightedRandomChooser<EntityType, Integer> TYPES = new ImmutableWeightedRandomChooser<>(TYPE_MAP);

    final static Range<Integer> AMOUNT = Range.closed(1, 3);

    public MobsMutation(Match match) {
        super(match, false);
    }

    @Override
    public void kits(MatchPlayer player, List<Kit> kits) {
        super.kits(player, kits);
        int eggs = entropy().randomInt(AMOUNT);
        for(int i = 0; i < eggs; i++) {
            ItemStack item = item(Material.MONSTER_EGG, entropy().randomInt(AMOUNT));
            SpawnEggMeta egg = (SpawnEggMeta) item.getItemMeta();
            egg.setSpawnedType(TYPES.choose(entropy()));
            item.setItemMeta(egg);
            kits.add(new FreeItemKit(item));
        }
    }

}
