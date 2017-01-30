package tc.oc.commons.core.inject;

import java.util.concurrent.ThreadFactory;
import javax.inject.Singleton;

import com.google.inject.Provides;
import com.google.inject.Stage;
import com.google.inject.TypeLiteral;
import tc.oc.commons.core.exception.ExceptionHandler;
import tc.oc.commons.core.exception.LoggingExceptionHandler;
import tc.oc.commons.core.exception.NamedThreadFactory;
import tc.oc.commons.core.util.NumberFactory;
import tc.oc.commons.core.util.SystemFutureCallback;
import tc.oc.evil.DecoratorFactory;
import tc.oc.parse.ParsersManifest;

/**
 * Configures low-level utility classes, nothing related to Minecraft
 */
public class UtilCoreManifest extends Manifest {
    @Override
    protected void configure() {
        // @InjectorScoped annotation
        install(new InjectorScopeModule());

        // Provide a global, catch-all exception handler
        bind(LoggingExceptionHandler.class).in(Singleton.class);
        bind(ExceptionHandler.class).to(LoggingExceptionHandler.class);
        bind(new TypeLiteral<ExceptionHandler<Throwable>>(){}).to(LoggingExceptionHandler.class);
        bind(Thread.UncaughtExceptionHandler.class).to(LoggingExceptionHandler.class);

        // Evil decorator thing
        // Create it manually so we can use it before the injector is ready
        bind(DecoratorFactory.class).toInstance(DecoratorFactory.get());

        install(new NumberFactory.Manifest());

        install(new ParsersManifest());

        requestStaticInjection(SystemFutureCallback.class);

        if(currentStage() == Stage.DEVELOPMENT) {
            // This is useful, but it makes the LeakDetector unhappy
            //install(new RepeatInjectionDetector());
        }
    }

    @Provides @Singleton
    ThreadFactory threadFactory(Thread.UncaughtExceptionHandler exceptionHandler) {
        return runnable -> {
            final Thread thread = new Thread(runnable);
            thread.setUncaughtExceptionHandler(exceptionHandler);
            return thread;
        };
    }

    @Provides @Singleton
    NamedThreadFactory namedThreadFactory(ThreadFactory factory) {
        return (name, code) -> {
            final Thread thread = factory.newThread(code);
            thread.setName(name);
            return thread;
        };
    }
}
