package tc.oc.minecraft.analytics;

import tc.oc.analytics.TaggerBinder;
import tc.oc.commons.core.inject.HybridManifest;

public class MinecraftAnalyticsManifest extends HybridManifest {
    @Override
    protected void configure() {
        final TaggerBinder taggers = new TaggerBinder(publicBinder());
        taggers.addBinding().to(ServerTagger.class);
    }
}
