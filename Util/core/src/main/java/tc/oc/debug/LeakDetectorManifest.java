package tc.oc.debug;

import com.google.inject.Provides;
import com.google.inject.Stage;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.minecraft.api.configuration.Configuration;

public class LeakDetectorManifest extends HybridManifest {

    @Override
    protected void configure() {
        bindAndExpose(LeakDetector.class).to(LeakDetectorImpl.class);
    }

    @Provides
    LeakDetectorConfig leakDetectorConfig(Configuration config, Stage stage) {
        return () -> config.getBoolean("leak-detector.enabled", stage == Stage.DEVELOPMENT);
    }
}
