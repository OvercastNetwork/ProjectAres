package tc.oc.pgm.animation;

import org.bukkit.World;
import org.bukkit.block.BlockImage;
import org.bukkit.geometry.Cuboid;
import org.bukkit.region.BlockRegion;
import org.bukkit.region.CuboidBlockRegion;
import org.bukkit.util.ImVector;
import org.bukkit.util.Vector;
import tc.oc.pgm.features.FeatureDefinition;
import tc.oc.pgm.features.FeatureFactory;
import tc.oc.pgm.features.FeatureInfo;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.regions.Region;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

@FeatureInfo(name = "frame")
public interface  FrameDefinition extends FeatureDefinition, FeatureFactory<Frame> {

    Vector origin();

    Region region();

    Cuboid bounds();

    boolean includeAir();

    boolean clearSource();

    BlockRegion staticBlocks();
}

class FrameDefinitionImpl extends FeatureDefinition.Impl implements FrameDefinition {

    private final @Inspect Region region;
    private final @Inspect boolean includeAir;
    private final @Inspect boolean clearSource;

    // Lazy init because of feature proxies
    private @Nullable ImVector origin;
    private Cuboid bounds;
    private BlockRegion staticBlocks;

    public FrameDefinitionImpl(@Nullable Vector origin, Region region, boolean includeAir, boolean clearSource) {
        this.origin = origin == null ? null : ImVector.copyOf(origin);
        this.region = checkNotNull(region);
        this.includeAir = includeAir;
        this.clearSource = clearSource;
    }

    @Override
    public Vector origin() {
        if(origin == null) {
            origin = region.getBounds().minimum();
        }
        return origin;
    }

    @Override
    public Region region() {
        return region;
    }

    @Override
    public boolean includeAir() {
        return includeAir;
    }

    @Override
    public boolean clearSource() {
        return clearSource;
    }

    @Override
    public Cuboid bounds() {
        if(bounds == null) {
            bounds = region.getBounds();
        }
        return bounds;
    }

    @Override
    public BlockRegion staticBlocks() {
        if(staticBlocks == null) {
            this.staticBlocks = CuboidBlockRegion.fromMinAndSize(bounds().minimumBlockInside(),
                                                                 bounds().blockSize());
        }
        return staticBlocks;
    }

    @Override
    public void load(Match match) {
        match.features().get(this);
    }

    @Override
    public Frame createFeature(Match match) {
        return new FrameImpl(match.getWorld());
    }

    class FrameImpl implements Frame {
        private final BlockImage image;
        private Vector origin;

        FrameImpl(World world) {
            this.image = world.copyBlocks(staticBlocks(),
                                          includeAir(),
                                          clearSource());
            this.origin = ImVector.ofZero();
        }

        @Override
        public FrameDefinition getDefinition() {
            return tc.oc.pgm.animation.FrameDefinitionImpl.this;
        }

        @Override
        public void place(World world, Vector newLocation) {
            world.pasteBlocks(image, newLocation.minus(origin));
        }

        @Override
        public Vector getOrigin() {
            return origin;
        }

        @Override
        public void setOrigin(Vector origin) {
            this.origin = origin;
        }
    }
}
