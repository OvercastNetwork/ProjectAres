package tc.oc.pgm.animation;

import com.google.inject.assistedinject.Assisted;
import org.bukkit.World;
import org.bukkit.util.ImVector;
import tc.oc.commons.core.inject.InnerFactory;
import tc.oc.pgm.features.FeatureDefinition;
import tc.oc.pgm.features.FeatureFactory;
import tc.oc.pgm.features.FeatureInfo;
import tc.oc.pgm.features.MatchFeatureContext;
import tc.oc.pgm.filters.FilterMatchModule;
import tc.oc.pgm.match.Match;

import javax.inject.Inject;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

@FeatureInfo(name = "animation")
public interface AnimationDefinition extends FeatureDefinition, FeatureFactory<Animation> {}

class AnimationDefinitionImpl extends FeatureDefinition.Impl implements AnimationDefinition {

    interface Factory {
        AnimationDefinitionImpl create(List<FrameDefinition> frames,
                                       @Assisted("after") Duration after,
                                       @Assisted("loop") Duration loop,
                                       @Assisted("count") int count,
                                       @Assisted("position") Optional<ImVector> position);
    }

    final @Inspect List<FrameDefinition> frames;
    final @Inspect Duration after;
    final @Inspect Duration loop;
    final @Inspect int count;
    final @Inspect Optional<ImVector> position;

    private final InnerFactory<AnimationDefinitionImpl, AnimationImpl> factory;

    @Inject
    AnimationDefinitionImpl(@Assisted List<FrameDefinition> frames,
                            @Assisted("after") Duration after,
                            @Assisted("loop") Duration loop,
                            @Assisted("count") int count,
                            @Assisted("position") Optional<ImVector> position,
                            InnerFactory<AnimationDefinitionImpl, AnimationImpl> factory) {

        this.frames = checkNotNull(frames);
        this.after = after;
        this.loop = loop;
        this.count = count;
        this.position = position;
        this.factory = factory;
    }

    @Override
    public AnimationImpl createFeature(Match match) {
        return factory.create(this);
    }

    public void place(Frame frame, World world, ImVector offset) {
        frame.place(world, offset);
    }

    @Override
    public void load(Match match) {
        match.features().get(this);
    }

    class AnimationImpl implements Animation {

        final World world;
        final Duration after;
        final Duration loop;
        final int count;
        final List<Frame> frames;

        @Inject AnimationImpl(World world, MatchFeatureContext features, AnimationScheduler scheduler, FilterMatchModule fmm) {
            this.world = world;
            final AnimationDefinitionImpl def = AnimationDefinitionImpl.this;
            this.after = def.after;
            this.loop = def.loop;
            this.count = def.count;
            this.frames = new ArrayList<>();
            for (FrameDefinition frameDef : def.frames) {
                Frame frame = features.get(frameDef);
                frame.setOrigin(frameDef.origin());
                frames.add(frame);
            }
            scheduler.animations.add(this);
        }

        @Override
        public AnimationDefinition getDefinition() {
            return AnimationDefinitionImpl.this;
        }

        @Override
        public void place(Frame frame) {
            frame.place(world, position.get().copy());
        }

        @Override
        public World getWorld() {
            return world;
        }

        @Override
        public Duration getAfter() {
            return after;
        }

        @Override
        public Duration getLoop() {
            return loop;
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public List<Frame> getFrames() {
            return frames;
        }
    }
}
