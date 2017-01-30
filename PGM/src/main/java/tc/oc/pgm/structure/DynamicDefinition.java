package tc.oc.pgm.structure;

import java.util.Optional;
import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import org.bukkit.World;
import org.bukkit.block.BlockImage;
import org.bukkit.geometry.CoarseTransform;
import org.bukkit.util.ImVector;
import tc.oc.commons.core.inject.InnerFactory;
import tc.oc.pgm.features.FeatureDefinition;
import tc.oc.pgm.features.FeatureFactory;
import tc.oc.pgm.features.FeatureInfo;
import tc.oc.pgm.features.MatchFeatureContext;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.FilterMatchModule;
import tc.oc.pgm.filters.query.TransientQuery;
import tc.oc.pgm.match.Match;

import static com.google.common.base.Preconditions.checkNotNull;

@FeatureInfo(name = "dynamic")
public interface DynamicDefinition extends FeatureDefinition, FeatureFactory<Dynamic> {}

class DynamicDefinitionImpl extends FeatureDefinition.Impl implements DynamicDefinition {

    interface Factory {
        DynamicDefinitionImpl create(StructureDefinition structure,
                                     @Assisted("trigger") Filter trigger,
                                     @Assisted("passive") Filter passive,
                                     @Assisted("position") Optional<ImVector> position,
                                     @Assisted("offset") Optional<ImVector> offset);
    }

    private final @Inspect StructureDefinition structure;
    private final @Inspect Filter trigger;
    private final @Inspect Filter passive;
    private final @Inspect Optional<ImVector> position;
    private final @Inspect Optional<ImVector> offset;

    private final InnerFactory<DynamicDefinitionImpl, DynamicImpl> factory;

    @Inject DynamicDefinitionImpl(@Assisted StructureDefinition structure,
                                  @Assisted("trigger") Filter trigger,
                                  @Assisted("passive") Filter passive,
                                  @Assisted("position") Optional<ImVector> position,
                                  @Assisted("offset") Optional<ImVector> offset,
                                  InnerFactory<DynamicDefinitionImpl, DynamicImpl> factory) {

        this.structure = checkNotNull(structure);
        this.trigger = checkNotNull(trigger);
        this.passive = checkNotNull(passive);
        this.position = position;
        this.offset = offset;
        this.factory = factory;
    }

    @Override
    public Dynamic createFeature(Match match) {
        return factory.create(this);
    }

    @Override
    public void load(Match match) {
        match.features().get(this);
    }

    class DynamicImpl implements Dynamic {

        final World world;
        final Structure structure;
        final ImVector offset;
        final BlockImage clearImage;

        // Since the passive filter can skip placing the structure,
        // we need to keep track of whether its placed or not if we
        // want to avoid unnecessary clears.
        boolean placed;

        @Inject DynamicImpl(Match match, World world, MatchFeatureContext features, DynamicScheduler scheduler, FilterMatchModule fmm) {
            this.world = world;
            final DynamicDefinitionImpl def = DynamicDefinitionImpl.this;
            this.structure = features.get(def.structure);
            this.offset = position.map(p -> p.minus(def.structure.origin()))
                                  .orElse(def.offset.orElse(ImVector.ofZero()));
            this.clearImage = world.copyBlocks(structure.dynamicBlocks().transform(CoarseTransform.translation(offset)), true, false);

            fmm.onChange(Match.class, trigger, (m, response) -> {
                if(response) {
                    // This is the passive filter, not the dynamic one
                    if(passive.query(new TransientQuery(match)).toBoolean(true)) {
                        scheduler.queuePlace(this);
                    }
                } else {
                    scheduler.queueClear(this);
                }
            });
        }

        @Override
        public DynamicDefinition getDefinition() {
            return DynamicDefinitionImpl.this;
        }

        @Override
        public void place() {
            if(!placed) {
                placed = true;
                structure.place(world, offset);
            }
        }

        @Override
        public void clear() {
            if(placed) {
                placed = false;
                world.pasteBlocks(clearImage);
            }
        }
    }
}
