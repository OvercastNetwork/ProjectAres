package tc.oc.pgm.filters.matcher.match;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;
import tc.oc.commons.bukkit.localization.MessageTemplate;
import tc.oc.time.PeriodConverters;
import tc.oc.time.PeriodRenderers;
import tc.oc.commons.core.util.Comparables;
import tc.oc.commons.core.util.MapUtils;
import tc.oc.commons.core.util.TimeUtils;
import tc.oc.pgm.bossbar.BossBarMatchModule;
import tc.oc.pgm.countdowns.CountdownBossBarSource;
import tc.oc.pgm.features.Feature;
import tc.oc.pgm.features.FeatureDefinitionContext;
import tc.oc.pgm.features.FeatureFactory;
import tc.oc.pgm.features.FeatureValidationContext;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.FilterMatchModule;
import tc.oc.pgm.filters.Filterable;
import tc.oc.pgm.filters.Filterables;
import tc.oc.pgm.filters.matcher.TypedFilter;
import tc.oc.pgm.filters.operator.SingleFilterFunction;
import tc.oc.pgm.filters.parser.DynamicFilterValidation;
import tc.oc.pgm.filters.parser.RespondsToQueryValidation;
import tc.oc.pgm.filters.query.IMatchQuery;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.Repeatable;
import tc.oc.pgm.xml.InvalidXMLException;

/**
 * Filter function that is low when its operand is low, and high only for
 * a specified time after its operand goes high.
 */
public class MonostableFilter extends SingleFilterFunction implements TypedFilter<IMatchQuery>,
                                                                      FeatureFactory<MonostableFilter.Reactor<?>> {
    private final @Inspect Duration duration;
    private final @Inspect Optional<MessageTemplate> message;
    private final boolean colons;

    public MonostableFilter(Duration duration, Filter trigger, Optional<MessageTemplate> message) {
        super(trigger);
        this.duration = duration;
        this.message = message;
        this.colons = Comparables.greaterOrEqual(duration, Duration.ofSeconds(90));
    }

    public static Filter after(FeatureDefinitionContext context, Duration time) throws InvalidXMLException {
        return MatchStateFilter.running().and(
            // Must be in the FDC so load() is called
            context.define(Filter.class,
                new MonostableFilter(
                    time,
                    MatchStateFilter.running(),
                    Optional.empty()
                )
            ).not()
        );
    }

    @Override
    public void validate(FeatureValidationContext context) throws InvalidXMLException {
        context.validate(filter, DynamicFilterValidation.INSTANCE);
        context.validate(filter, RespondsToQueryValidation.get(message.isPresent() ? MatchPlayer.class : Match.class));
    }

    @Override
    public void load(Match match) {
        match.feature(this);
    }

    @Override
    public Reactor<?> createFeature(Match match) {
        return new Reactor<>(match, Filterables.scope(filter));
    }

    @Override
    public boolean matches(IMatchQuery query) {
        return query.feature(this).matches(query);
    }

    class Reactor<F extends Filterable<?>> implements Feature<MonostableFilter> {

        final Match match;
        final FilterMatchModule fmm;
        final BossBarMatchModule bbmm;

        final Class<F> scope;
        final Optional<Bar> bar;

        // Filterables that currently pass the inner filter, mapped to the instants that they expire.
        // They are not actually removed until the inner filter goes false.
        final Map<F, Instant> endTimes = new HashMap<>();

        Instant lastTick = TimeUtils.INF_PAST;

        Reactor(Match match, Class<F> scope) {
            this.match = match;
            this.scope = scope;
            this.bbmm = match.needMatchModule(BossBarMatchModule.class);
            this.fmm = match.needMatchModule(FilterMatchModule.class);

            fmm.onChange(scope, filter, this::matches);

            bar = message.map(Bar::new);
            bar.ifPresent(bar -> {
                // If a countdown message is specified, register a global BossBarSource.
                // Every player will have a view of this source, but it will hide itself
                // at rendering time for viewers that do not pass the filter.
                bbmm.add(bar);

                // Invalidate the bar when this filter (not the inner filter) changes for any player.
                // It's easier to do this with a seperate listener, rather than trying to do it in the
                // trigger listener, because that listener may be at a more general scope and won't always
                // be called when the filter changes for individual players, e.g. if it's party scoped and
                // a player changes parties.
                fmm.onChange(MatchPlayer.class, MonostableFilter.this, (player, response) -> {
                    bbmm.invalidate(bar, player.getBukkit());
                });
            });
        }

        void invalidate(F filterable) {
            fmm.invalidate(getDefinition(), filterable);
        }

        boolean matches(IMatchQuery query) {
            return query.filterable(scope)
                        .filter(filterable -> matches(filterable, filter.response(query)))
                        .isPresent();
        }

        boolean matches(F filterable, boolean response) {
            if(response) {
                final Instant now = match.getInstantNow();
                final Instant end = endTimes.computeIfAbsent(filterable, f -> {
                    invalidate(filterable);
                    return now.plus(duration);
                });
                return now.isBefore(end);
            } else {
                if(endTimes.remove(filterable) != null) {
                    invalidate(filterable);
                }
                return false;
            }
        }

        @Repeatable
        void tick() {
            final Instant now = match.getInstantNow();

            endTimes.forEach((filterable, end) -> {
                if(now.isBefore(end)) {
                    // If the entry is still valid, check if its elapsed time crossed a second
                    // boundary over the last tick, and invalidate the boss bar if it did.
                    bar.ifPresent(bar -> {
                        if(Duration.between(lastTick, end).getSeconds() != Duration.between(now, end).getSeconds()) {
                            filterable.filterableDescendants(MatchPlayer.class)
                                      .forEach(player -> bbmm.invalidate(bar, player.getBukkit()));
                        }
                    });
                } else if(lastTick.isBefore(end)) {
                    // If entry is expired, but was not expired last tick, invalidate this filter
                    invalidate(filterable);
                }
            });

            lastTick = now;
        }

        @Override
        public MonostableFilter getDefinition() {
            return MonostableFilter.this;
        }

        class Bar extends CountdownBossBarSource {
            final MessageTemplate message;

            Bar(MessageTemplate message) {
                super(duration, ChatColor.YELLOW, ChatColor.AQUA,
                      colons ? PeriodConverters.normalized() : PeriodConverters.seconds(),
                      colons ? PeriodRenderers.colons() : PeriodRenderers.natural());

                this.message = message;
            }

            @Override
            protected MessageTemplate barMessage(Player viewer) {
                return message;
            }

            @Override
            protected Optional<Duration> barTime(Player viewer) {
                return match.player(viewer)
                            .flatMap(player -> player.filterableAncestor(scope))
                            .flatMap(filterable -> MapUtils.value(endTimes, filterable))
                            .flatMap(end -> TimeUtils.positiveDuration(match.getInstantNow(), end));
            }

            @Override
            public BarColor barColor(Player viewer) {
                return BarColor.YELLOW;
            }
        }
    }
}

