package tc.oc.pgm.points;

import java.util.List;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import tc.oc.commons.bukkit.util.Materials;
import tc.oc.commons.bukkit.util.WorldBorderUtils;
import tc.oc.commons.core.inspect.Inspectable;
import tc.oc.commons.core.stream.Collectors;
import tc.oc.commons.core.util.Lazy;
import tc.oc.commons.core.random.RandomUtils;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.regions.Region;
import tc.oc.pgm.regions.Union;

public class RegionPointProvider extends Inspectable.Impl implements PointProvider {

    private final @Inspect Region region;
    private final @Inspect PointProviderAttributes attributes;

    /**
     * For convenience, union point-providers are treated as multi-region point providers,
     * i.e. a single sub-region is chosen at random. We can't expand unions until after
     * parsing though, so we have to do it lazily here instead of in the parser.
     */
    private final Lazy<List<Region>> expandedRegions = Lazy.from(
        () -> Union.expand(getRegion()).collect(Collectors.toImmutableList())
    );

    public RegionPointProvider(Region region, PointProviderAttributes attributes) {
        this.attributes = attributes;
        this.region = checkNotNull(region, "region");;
    }

    @Override
    public Region getRegion() {
        return region;
    }

    @Override
    public int getWeight() {
        return expandedRegions.get().size();
    }

    @Override
    public boolean canFail() {
        return attributes.isSafe();
    }

    @Override
    public Location getPoint(Match match, @Nullable Entity entity) {
        final Region region = RandomUtils.element(match.getRandom(), expandedRegions.get());
        final Vector pos = region.getRandom(match.getRandom());
        PointProviderLocation location = new PointProviderLocation(match.getWorld(), pos);

        if(attributes.getYawProvider() != null) {
            location.setYaw(attributes.getYawProvider().getAngle(pos));
        }

        if(attributes.getPitchProvider() != null) {
            location.setPitch(attributes.getPitchProvider().getAngle(pos));
        }

        location = makeSafe(location);

        return location;
    }

    private PointProviderLocation makeSafe(PointProviderLocation location) {
        if(location == null) return null;

        // If the initial point is safe, just return it
        if(isSpawnable(location)) return location;

        // Try centering the point in its block
        location = location.clone();
        location.setX(location.getBlockX() + 0.5);
        location.setY(location.getBlockY());
        location.setZ(location.getBlockZ() + 0.5);
        if(isSpawnable(location)) return location;

        int scanDirection;
        if(attributes.isOutdoors()) {
            location.setY(Math.max(location.getY(), location.getWorld().getHighestBlockYAt(location)));
            scanDirection = 1;
        } else {
            scanDirection = -1;
        }

        // Scan downward, then upward, for a safe point in the region. If spawn is outdoors, just scan upward.
        for(; scanDirection <= 1; scanDirection += 2) {
            for(PointProviderLocation safe = location.clone();
                safe.getBlockY() >= 0 && safe.getBlockY() < 256 && region.contains(safe);
                safe.setY(safe.getBlockY() + scanDirection)) {

                if(isSpawnable(safe)) return safe;
            }
        }

        // Give up
        return null;
    }

    private boolean isSpawnable(Location location) {
        if(attributes.isSafe() && !isSafe(location)) return false;
        if(attributes.isOutdoors() && !isOutdoors(location)) return false;
        return true;
    }

    /**
     * Indicates whether or not this spawn is safe.
     *
     * @param location Location to check for.
     * @return True or false depending on whether this is a safe spawn point.
     */
    private boolean isSafe(Location location) {
        if(!WorldBorderUtils.isInsideBorder(location)) return false;

        Block block = location.getBlock();
        Block above = block.getRelative(BlockFace.UP);
        Block below = block.getRelative(BlockFace.DOWN);

        return block.isEmpty() && above.isEmpty() && Materials.isColliding(below.getType());
    }

    private boolean isOutdoors(Location location) {
        return location.getWorld().getHighestBlockYAt(location) <= location.getBlockY();
    }
}
