package tc.oc.bukkit.analytics;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import tc.oc.analytics.Count;
import tc.oc.analytics.Gauge;
import tc.oc.analytics.MetricFactory;
import tc.oc.commons.bukkit.util.NMSHacks;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.minecraft.analytics.AnalyticsFacet;
import tc.oc.minecraft.api.scheduler.Tickable;

public class TickReporter extends AnalyticsFacet implements PluginFacet, Tickable {

    private static final Duration INITIAL_DELAY = Duration.ofSeconds(10);

    private final Count tickCount;
    private final Gauge tickDuration;
    private final Gauge tickInterval;

    private long lastTickNanos = Long.MIN_VALUE;

    @Inject TickReporter(MetricFactory metrics) {
        tickCount = metrics.count("bukkit.ticks");
        tickDuration = metrics.gauge("bukkit.tick_duration");
        tickInterval = metrics.gauge("bukkit.tick_interval");
    }

    @Override
    public Duration initialDelay() {
        return INITIAL_DELAY;
    }

    @Override
    public void tick() {
        tickDuration.measure((double) NMSHacks.lastTickDurationNanos() / TimeUnit.MILLISECONDS.toNanos(1));
        tickCount.increment();

        final long now = System.nanoTime();
        if(lastTickNanos > Long.MIN_VALUE) {
            tickInterval.measure((double) (now - lastTickNanos) / TimeUnit.MILLISECONDS.toNanos(1));
        }
        lastTickNanos = now;
    }
}
