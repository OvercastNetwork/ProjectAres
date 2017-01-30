package tc.oc.pgm.kits;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tc.oc.commons.core.util.Optionals;
import tc.oc.pgm.compose.All;
import tc.oc.pgm.compose.Composition;
import tc.oc.pgm.compose.None;
import tc.oc.pgm.compose.Unit;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.matcher.StaticFilter;
import tc.oc.pgm.filters.query.TransientPlayerQuery;
import tc.oc.pgm.match.MatchPlayer;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Optional.empty;

public interface KitNode extends Kit {

    static KitNode of(Stream<Kit> kits) {
        return new KitNodeImpl(Stream.empty(), new All<>(kits.map(Unit::new)), StaticFilter.ALLOW, empty(), empty());
    }

    static KitNode of(Kit... kits) {
        return of(Stream.of(kits));
    }

    static KitNode of(Collection<Kit> kits) {
        return of(kits.stream());
    }

    KitNode EMPTY = new KitNodeImpl(Stream.empty(), new None<>(), StaticFilter.ALLOW, empty(), empty());
}

class KitNodeImpl extends Kit.Impl implements KitNode, Kit {
    private final @Inspect List<Kit> parents;
    private final @Inspect Composition<Kit> kits;
    private final @Inspect Filter filter;
    private final @Inspect Optional<Boolean> force;
    private final @Inspect Optional<Boolean> potionParticles;

    public KitNodeImpl(Stream<Kit> parents, Composition<Kit> kits, Filter filter, Optional<Boolean> force, Optional<Boolean> potionParticles) {
        this.parents = parents.collect(Collectors.toList());
        this.kits = kits;
        this.filter = checkNotNull(filter);
        this.force = force;
        this.potionParticles = potionParticles;
    }

    @Inspect private Optional<Filter> filter() {
        // Hide default value
        return Optionals.filter(filter, f -> !Objects.equals(f, StaticFilter.ALLOW));
    }

    @Override
    public Stream<? extends Kit> dependencies() {
        return Stream.concat(parents.stream(), kits.dependencies());
    }

    @Override
    public void apply(MatchPlayer player, boolean force, ItemKitApplicator items) {
        if(this.filter.query(player).isAllowed()) {
            for(Kit kit : parents) {
                kit.apply(player, this.force.orElse(force), items);
            }
            kits.elements(new TransientPlayerQuery(player)).forEach(
                kit -> kit.apply(player, this.force.orElse(force), items)
            );
            potionParticles.ifPresent(player.getBukkit()::setPotionParticles);
        }
    }

    @Override
    public boolean isRemovable() {
        return kits.isConstant();
    }

    @Override
    public void remove(MatchPlayer player) {
        kits.elements(new TransientPlayerQuery(player)).forEach(
            kit -> kit.remove(player)
        );
    }
}
