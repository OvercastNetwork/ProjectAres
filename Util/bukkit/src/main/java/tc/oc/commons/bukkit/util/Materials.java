package tc.oc.commons.bukkit.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public abstract class Materials {

    /**
     * Is the given {@link Material} a block that collides with entities?
     *
     * Note that this is not always 100% correct. There are a few blocks for which
     * solidness depends on state, such as fence gates.
     */
    public static boolean isColliding(Material material) {
        if(material == null) {
            return false;
        }

        switch(material) {
            // Missing from Bukkit
            case CARPET:
            case WATER_LILY:
                return true;

            // Incorrectly included by Bukkit
            case SIGN_POST:
            case WALL_SIGN:
            case WOOD_PLATE:
            case STONE_PLATE:
            case IRON_PLATE:
            case GOLD_PLATE:
            case STANDING_BANNER:
            case WALL_BANNER:
                return false;

            default:
                return material.isSolid();
        }
    }

    public static boolean isColliding(MaterialData material) {
        return isColliding(material.getItemType());
    }

    public static boolean isColliding(BlockState block) {
        return isColliding(block.getMaterial());
    }

    /**
     * Does the given {@link Material} support falling blocks placed on top of it?
     *
     * This only counts when the gravity block is placed directly on top of the given material,
     * not when it is already falling and lands on the material.
     */
    public static boolean canSupportBlocks(Material material) {
        if(material == null || !material.isBlock() || isLiquid(material)) return false;

        switch(material) {
            case AIR:
            case FIRE:
                return false;
        }

        return true;
    }

    public static boolean isWater(Material material) {
        return material == Material.WATER || material == Material.STATIONARY_WATER;
    }

    public static boolean isWater(MaterialData material) {
        return isWater(material.getItemType());
    }

    public static boolean isWater(Location location) {
        return isWater(location.getBlock().getType());
    }

    public static boolean isWater(BlockState block) {
        return isWater(block.getMaterial());
    }

    public static boolean isLava(Material material) {
        return material == Material.LAVA || material == Material.STATIONARY_LAVA;
    }

    public static boolean isLava(MaterialData material) {
        return isLava(material.getItemType());
    }

    public static boolean isLava(Location location) {
        return isLava(location.getBlock().getType());
    }

    public static boolean isLava(BlockState block) {
        return isLava(block.getMaterial());
    }

    public static boolean isLiquid(Material material) {
        return isWater(material) || isLava(material);
    }

    public static boolean isClimbable(Material material) {
        return material == Material.LADDER || material == Material.VINE;
    }

    public static boolean isClimbable(Location location) {
        return isClimbable(location.getBlock().getType());
    }

    public static boolean isBucket(ItemStack bucket) {
        return isBucket(bucket.getType());
    }

    public static boolean isBucket(Material bucket) {
        return bucket == Material.BUCKET || bucket == Material.LAVA_BUCKET || bucket == Material.WATER_BUCKET || bucket == Material.MILK_BUCKET;
    }

    public static boolean isSign(Material material) {
        return material == Material.SIGN_POST || material == Material.WALL_SIGN;
    }

    public static boolean isChest(Material material) {
        return material == Material.CHEST || material == Material.TRAPPED_CHEST;
    }

    public static Material materialInBucket(ItemStack bucket) {
        return materialInBucket(bucket.getType());
    }

    public static Material materialInBucket(Material bucket) {
        switch(bucket) {
            case BUCKET:
            case MILK_BUCKET:
                return Material.AIR;

            case LAVA_BUCKET: return Material.LAVA;
            case WATER_BUCKET: return Material.WATER;

            default: throw new IllegalArgumentException(bucket + " is not a bucket");
        }
    }

    public static Material materialAt(Location location) {
        Block block = location.getBlock();
        return block == null ? Material.AIR : block.getType();
    }
}
