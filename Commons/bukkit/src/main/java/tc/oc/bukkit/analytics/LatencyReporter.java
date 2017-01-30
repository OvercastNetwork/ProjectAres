package tc.oc.bukkit.analytics;

import java.time.Duration;
import javax.inject.Inject;

import tc.oc.analytics.Gauge;
import tc.oc.analytics.MetricFactory;
import tc.oc.api.bukkit.users.OnlinePlayers;
import tc.oc.commons.bukkit.util.NMSHacks;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.commons.core.util.Numbers;
import tc.oc.minecraft.analytics.AnalyticsFacet;
import tc.oc.minecraft.api.scheduler.Tickable;

public class LatencyReporter extends AnalyticsFacet implements PluginFacet, Tickable {

    private final OnlinePlayers onlinePlayers;
    private final Gauge latency;

    @Inject LatencyReporter(OnlinePlayers onlinePlayers, MetricFactory metrics) {
        this.onlinePlayers = onlinePlayers;
        this.latency = metrics.gauge("bukkit.latency");
    }

    @Override
    public Duration tickPeriod() {
        return Duration.ofSeconds(10);
    }

    @Override
    public void tick() {
        onlinePlayers.all()
                     .stream()
                     .mapToInt(NMSHacks::playerLatencyMillis)
                     .average()
                     .ifPresent(average -> latency.measure(Numbers.clamp(
                         average,
                         0, 2000 // Filter out insane values so our graphs don't get wrecked
                     )));
    }
}
