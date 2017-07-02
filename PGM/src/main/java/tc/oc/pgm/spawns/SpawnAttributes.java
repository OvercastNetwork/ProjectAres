package tc.oc.pgm.spawns;

import java.util.Objects;
import java.util.Optional;

import tc.oc.commons.core.inspect.Inspectable;
import tc.oc.commons.core.util.Optionals;
import tc.oc.commons.core.util.Utils;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.matcher.StaticFilter;
import tc.oc.pgm.filters.operator.AllFilter;
import tc.oc.pgm.kits.Kit;
import tc.oc.pgm.points.PointProviderAttributes;

import static com.google.common.base.Preconditions.checkNotNull;

public class SpawnAttributes extends Inspectable.Impl {

    public final @Inspect PointProviderAttributes providerAttributes;
    public final @Inspect Filter filter;
    public final @Inspect Optional<Kit> kit;
    public final @Inspect boolean sequential;
    public final @Inspect boolean spread;
    public final @Inspect boolean exclusive;
    public final @Inspect boolean persistent;
    public final @Inspect boolean useLastParticipatingLocation;

    public SpawnAttributes(PointProviderAttributes providerAttributes, Filter filter, Optional<Kit> kit, boolean sequential, boolean spread, boolean exclusive, boolean persistent, boolean useLastParticipatingLocation) {
        this.filter = checkNotNull(filter);
        this.providerAttributes = checkNotNull(providerAttributes);
        this.kit = checkNotNull(kit);
        this.sequential = sequential;
        this.spread = spread;
        this.exclusive = exclusive;
        this.persistent = persistent;
        this.useLastParticipatingLocation = useLastParticipatingLocation;
    }

    public SpawnAttributes() {
        this(new PointProviderAttributes(), StaticFilter.ABSTAIN, Optional.empty(), false, false, false, false, false);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filter, providerAttributes, kit, sequential, spread, exclusive, persistent);
    }

    @Override
    public boolean equals(Object obj) {
        return Utils.equals(SpawnAttributes.class, this, obj, that ->
            this.filter.equals(that.filter) &&
            this.providerAttributes.equals(that.providerAttributes) &&
            this.kit.equals(that.kit) &&
            this.sequential == that.sequential &&
            this.spread == that.spread &&
            this.exclusive == that.exclusive &&
            this.persistent == that.persistent &&
            this.useLastParticipatingLocation == that.useLastParticipatingLocation
        );
    }

    public SpawnAttributes merge(PointProviderAttributes providerAttributes,
                                 Filter filter,
                                 Optional<Kit> kit,
                                 Optional<Boolean> sequential,
                                 Optional<Boolean> spread,
                                 Optional<Boolean> exclusive,
                                 Optional<Boolean> persistent,
                                 Optional<Boolean> useLastParticipatingLocation) {

        return new SpawnAttributes(providerAttributes,
                                   AllFilter.of(filter, this.filter),
                                   Optionals.first(kit, this.kit),
                                   sequential.orElse(this.sequential),
                                   spread.orElse(this.spread),
                                   exclusive.orElse(this.exclusive),
                                   persistent.orElse(this.persistent),
                                   useLastParticipatingLocation.orElse(this.useLastParticipatingLocation));
    }
}
