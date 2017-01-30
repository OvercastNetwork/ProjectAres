package tc.oc.commons.bukkit.util.materials;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.meta.BannerMeta;
import tc.oc.commons.bukkit.util.BlockFaces;

public class Banners {
    private Banners() {}

    public static BannerMeta getItemMeta(Banner block) {
        BannerMeta meta = (BannerMeta) Bukkit.getItemFactory().getItemMeta(Material.BANNER);
        meta.setBaseColor(block.getBaseColor());
        meta.setPatterns(block.getPatterns());
        return meta;
    }

    public static void applyToBlock(Banner block, BannerMeta meta) {
        block.setBaseColor(meta.getBaseColor());
        block.setPatterns(meta.getPatterns());
    }

    public static boolean placeStanding(Location location, BannerMeta meta) {
        Block block = location.getBlock();
        block.setType(Material.STANDING_BANNER, false);

        final BlockState state = block.getState();
        if(state instanceof Banner) {
            Banner banner = (Banner) block.getState();
            applyToBlock(banner, meta);

            org.bukkit.material.Banner material = (org.bukkit.material.Banner) banner.getData();
            material.setFacingDirection(BlockFaces.yawToFace(location.getYaw()));
            banner.setData(material);
            banner.update(true);
            return true;
        }
        return false;
    }

    public static Location getLocationWithYaw(Banner block) {
        Location location = block.getLocation();
        location.setYaw(BlockFaces.faceToYaw(((org.bukkit.material.Banner) block.getData()).getFacing()));
        return location;
    }
}
