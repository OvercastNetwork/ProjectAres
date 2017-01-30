package tc.oc.pgm.fireworks;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.geometry.Cuboid;
import tc.oc.commons.bukkit.util.BlockUtils;
import tc.oc.commons.bukkit.util.BukkitUtils;
import tc.oc.pgm.controlpoint.events.ControllerChangeEvent;
import tc.oc.pgm.core.CoreLeakEvent;
import tc.oc.pgm.destroyable.DestroyableDestroyedEvent;
import tc.oc.pgm.flag.event.FlagCaptureEvent;
import tc.oc.pgm.regions.Region;
import tc.oc.pgm.wool.PlayerWoolPlaceEvent;

public class ObjectivesFireworkListener implements Listener {

    public void spawnFireworkDisplay(Location center, Color color, int count, double radius, int power) {
        FireworkEffect effect = FireworkEffect.builder().with(Type.BURST)
                                                        .withFlicker()
                                                        .withColor(color)
                                                        .withFade(Color.BLACK)
                                                        .build();

        for(int i = 0; i < count; i++) {
            double angle = 2 * Math.PI / count * i;
            double dx = radius * Math.cos(angle);
            double dz = radius * Math.sin(angle);
            Location baseLocation = center.clone().add(dx, 0, dz);

            Block block = baseLocation.getBlock();
            if(block == null || !block.getType().isOccluding()) {
                FireworkUtil.spawnFirework(FireworkUtil.getOpenSpaceAbove(baseLocation), effect, power);
            }
        }
    }

    public void spawnFireworkDisplay(World world, Region region, Color color, int count, double radiusMultiplier, int power) {
        final Cuboid bound = region.getBounds();
        final double radius = bound.maximum().minus(bound.minimum()).times(0.5).length();
        final Location center = bound.minimum().getMidpoint(bound.maximum()).toLocation(world);
        this.spawnFireworkDisplay(center, color, count, radiusMultiplier * radius, power);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWoolPlace(final PlayerWoolPlaceEvent event) {
        if(FireworksConfig.Goals.enabled() && event.getWool().isVisible()) {
            this.spawnFireworkDisplay(BlockUtils.center(event.getBlock()),
                                      event.getWool().getDyeColor().getColor(),
                                      6, 2, 2);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCoreLeak(final CoreLeakEvent event) {
        if(FireworksConfig.Goals.enabled() && event.getCore().isVisible()) {
            this.spawnFireworkDisplay(event.getMatch().getWorld(),
                                      event.getCore().getCasingRegion(),
                                      event.getCore().getColor(),
                                      8, 1.5, 2);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDestroyableBreak(final DestroyableDestroyedEvent event) {
        if(FireworksConfig.Goals.enabled() && event.getDestroyable().isVisible()) {
            this.spawnFireworkDisplay(event.getMatch().getWorld(),
                                      event.getDestroyable().getBlockRegion(),
                                      event.getDestroyable().getColor(),
                                      4, 3, 2);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onHillCapture(final ControllerChangeEvent event) {
        if(FireworksConfig.Goals.enabled() && event.getControlPoint().isVisible() && event.getNewController() != null) {
            this.spawnFireworkDisplay(event.getMatch().getWorld(),
                                      event.getControlPoint().getCaptureRegion(),
                                      BukkitUtils.colorOf(event.getNewController().getColor()),
                                      8, 1, 2);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFlagCapture(final FlagCaptureEvent event) {
        if(FireworksConfig.Goals.enabled() && event.getGoal().isVisible()) {
            this.spawnFireworkDisplay(event.getMatch().getWorld(),
                                      event.getNet().getRegion(),
                                      event.getGoal().getDyeColor().getColor(),
                                      6, 1, 2);
        }
    }
}
