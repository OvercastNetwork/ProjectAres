package tc.oc.analytics.datadog;

import java.util.Collection;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import com.google.inject.OutOfScopeException;
import com.timgroup.statsd.StatsDClient;
import tc.oc.analytics.AnalyticsClient;
import tc.oc.analytics.Event;
import tc.oc.analytics.Tag;
import tc.oc.analytics.Tagger;
import tc.oc.commons.core.inject.Injection;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.util.CacheUtils;
import tc.oc.minecraft.suspend.Suspendable;

class DataDogClient implements AnalyticsClient, Suspendable {

    private final Logger logger;
    private final DataDogConfig config;
    private final Provider<StatsDClient> clientProvider;

    private @Nullable StatsDClient client;

    // Provision taggers at the moment the tags are used, so they can be scoped.
    // We also use a provider for the entire collection to avoid circular deps.
    private final Provider<Collection<Provider<Tagger>>> taggers;

    private final LoadingCache<Tag, String> tagCache = CacheUtils.newWeakKeyCache(
        tag -> tag.name() + ":" + tag.value()
    );

    private final LoadingCache<ImmutableSet<Tag>, String> tagSetCache = CacheUtils.newWeakKeyCache(
        tags -> tags.stream()
                    .map(tagCache::getUnchecked)
                    .collect(Collectors.joining(","))
    );

    @Inject DataDogClient(Loggers loggers, DataDogConfig config, Provider<StatsDClient> clientProvider, Provider<Collection<Provider<Tagger>>> taggers) {
        this.logger = loggers.get(getClass());
        this.config = config;
        this.clientProvider = clientProvider;
        this.taggers = taggers;

        this.client = clientProvider.get();
    }

    @Override
    public boolean isActive() {
        return config.enabled();
    }

    private static final String[] EMPTY = new String[]{};

    String[] renderedTags() {
        final StringBuilder sb = new StringBuilder();
        boolean some = false;
        for(Provider<Tagger> provider : taggers.get()) {
            final Tagger tagger;
            try {
                tagger = Injection.unwrappingExceptions(OutOfScopeException.class, provider);
            } catch(OutOfScopeException e) {
                // If the tagger is out of scope, just omit its tags,
                // but log a warning in case this hides an unexpected exception.
                logger.warning("Ignoring out-of-scope tagger (" + e.toString() + ")");
                continue;
            }

            final ImmutableSet<Tag> tags = tagger.tags();
            if(!tags.isEmpty()) {
                if(some) sb.append(',');
                some = true;
                sb.append(tagSetCache.getUnchecked(tags));
            }
        }
        return some ? new String[] {sb.toString()} : EMPTY;
    }

    @Override
    public void count(String metric, int quantity) {
        if(client == null) return;
        client.count(metric, quantity, renderedTags());
    }

    @Override
    public void measure(String metric, double value) {
        if(client == null) return;
        client.gauge(metric, value, renderedTags());
    }

    @Override
    public void sample(String metric, double value) {
        if(client == null) return;
        client.histogram(metric, value, renderedTags());
    }

    @Override
    public void event(Event event) {
        if(client == null) return;
        client.recordEvent(
            com.timgroup.statsd.Event.builder()
                                     .withAlertType(alertType(event.level()))
                                     .withAggregationKey(event.key())
                                     .withTitle(event.title())
                                     .withText(event.body())
                                     .build()
        );
    }

    private static com.timgroup.statsd.Event.AlertType alertType(Event.Level level) {
        switch(level) {
            case SUCCESS: return com.timgroup.statsd.Event.AlertType.SUCCESS;
            case WARNING: return com.timgroup.statsd.Event.AlertType.WARNING;
            case ERROR: return com.timgroup.statsd.Event.AlertType.ERROR;
            default: return com.timgroup.statsd.Event.AlertType.INFO;
        }
    }

    @Override
    public void suspend() {
        client.stop();
        client = null;
    }

    @Override
    public void resume() {
        client = clientProvider.get();
    }
}
