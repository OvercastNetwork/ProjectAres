package tc.oc.analytics.datadog;

import javax.inject.Singleton;

import com.google.inject.Provides;
import com.timgroup.statsd.NoOpStatsDClient;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import tc.oc.analytics.AnalyticsClient;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.minecraft.suspend.SuspendableBinder;

public class DataDogManifest extends HybridManifest {

    @Override
    protected void configure() {
        bind(DataDogConfig.class);

        bind(AnalyticsClient.class)
            .to(DataDogClient.class);

        bind(DataDogClient.class).in(Singleton.class);
        expose(DataDogClient.class);
        new SuspendableBinder(publicBinder())
            .addBinding().to(DataDogClient.class);
    }

    @Provides
    StatsDClient statsDClient(DataDogConfig config) {
        return config.enabled()
               ? new NonBlockingStatsDClient(null, config.host(), config.port())
               : new NoOpStatsDClient();
    }
}
