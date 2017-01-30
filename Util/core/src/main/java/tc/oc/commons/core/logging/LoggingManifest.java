package tc.oc.commons.core.logging;

import tc.oc.commons.core.inject.HybridManifest;

public class LoggingManifest extends HybridManifest {

    @Override
    protected void configure() {
        publicBinder().bind(LoggingConfig.class)
                      .toInstance(new LoggingConfig());
    }
}
