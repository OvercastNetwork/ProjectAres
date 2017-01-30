package tc.oc.minecraft.analytics;

import javax.inject.Inject;

import tc.oc.analytics.AnalyticsClient;
import tc.oc.commons.core.plugin.PluginFacet;

public class AnalyticsFacet implements PluginFacet {

    @Inject private AnalyticsClient client;

    @Override
    public boolean isActive() {
        return client.isActive();
    }
}
