package tc.oc.commons.core.inject;

import java.util.logging.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.internal.Errors;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.ProvisionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

/**
 * Debugging tool - logs all injection activity (very spammy)
 */
public class InjectionLogger implements TypeListener, ProvisionListener {

    private final Logger logger;

    public InjectionLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
        logger.info("Encountered type " + type);
        encounter.register((InjectionListener<I>) injectee -> {
            logger.info("Injected " + injectee);
        });
    }

    @Override
    public <T> void onProvision(ProvisionInvocation<T> provision) {
        logger.info("Provisioning " + provision.provision() +
                    " for key " + provision.getBinding().getKey() +
                    " bound at " + Errors.convert(provision.getBinding().getSource()));
    }

    public static class Module extends AbstractModule {
        private final Logger logger;

        public Module(Logger logger) {
            this.logger = logger;
        }

        @Override
        protected void configure() {
            final InjectionLogger listener = new InjectionLogger(logger);
            bindListener(Matchers.any(), (TypeListener) listener);
            bindListener(Matchers.any(), (ProvisionListener) listener);
        }
    }
}
