package tc.oc.bungee.analytics;

import javax.inject.Inject;

import com.google.common.collect.ImmutableSet;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import tc.oc.analytics.AnalyticsClient;
import tc.oc.analytics.Count;
import tc.oc.analytics.DynamicTagger;
import tc.oc.analytics.Event;
import tc.oc.analytics.MetricFactory;
import tc.oc.analytics.Tag;
import tc.oc.minecraft.analytics.AnalyticsFacet;

public class PlayerTimeoutReporter extends AnalyticsFacet implements Listener {

    private final Event event = Event.error("player.timeout", "Player timeout");

    private final AnalyticsClient client;
    private final DynamicTagger tagger;
    private final Count timeouts;

    @Inject PlayerTimeoutReporter(AnalyticsClient client, MetricFactory metrics, DynamicTagger tagger) {
        this.client = client;
        this.timeouts = metrics.count("players.timeouts");
        this.tagger = tagger;
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        final Throwable exception = event.getPlayer().getDisconnectException();
        if(exception != null && "ReadTimeoutException".equals(exception.getClass().getSimpleName())) {
            tagger.withTags(
                ImmutableSet.of(
                    Tag.of("uuid", event.getPlayer().getUniqueId().toString()),
                    Tag.of("username", event.getPlayer().getName())
                ),
                () -> {
                    client.event(this.event.withBody(event.getPlayer().getName() + " timed out"));
                    timeouts.increment();
                }
            );
        }
    }
}
