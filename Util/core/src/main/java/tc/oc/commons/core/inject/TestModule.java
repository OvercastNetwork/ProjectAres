package tc.oc.commons.core.inject;

import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import tc.oc.commons.core.configuration.YamlConfiguration;
import tc.oc.commons.core.exception.ExceptionHandler;
import tc.oc.commons.core.exception.LoggingExceptionHandler;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.logging.SimpleLoggerFactory;
import tc.oc.minecraft.api.configuration.Configuration;

/**
 * TODO: Should this be in the test source root? That seems to make it unavailable to downstream modules.
 */
public class TestModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Loggers.class).to(SimpleLoggerFactory.class);
        bind(Configuration.class).to(YamlConfiguration.class);
        bind(ExceptionHandler.class).to(LoggingExceptionHandler.class).in(Singleton.class);


    }
}
