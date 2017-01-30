package tc.oc.minecraft.analytics;

import javax.inject.Inject;

import tc.oc.analytics.Count;
import tc.oc.analytics.Gauge;
import tc.oc.analytics.MetricFactory;
import tc.oc.api.minecraft.users.OnlinePlayers;
import tc.oc.commons.core.scheduler.Scheduler;

public class PlayerReporter extends AnalyticsFacet {

    @Inject private OnlinePlayers onlinePlayers;
    @Inject private Scheduler scheduler;

    private Gauge players;
    private Count joins;
    private Count leaves;

    @Inject void init(MetricFactory metrics) {
        players = metrics.gauge("players.players");
        joins = metrics.count("players.joins");
        leaves = metrics.count("players.leaves");
    }

    @Override
    public void enable() {
        update();
    }

    private void update() {
        scheduler.debounceTask(
            () -> players.measure(onlinePlayers.count())
        );
    }

    protected void join() {
        joins.increment();
        update();
    }

    protected void leave() {
        leaves.increment();
        update();
    }
}
