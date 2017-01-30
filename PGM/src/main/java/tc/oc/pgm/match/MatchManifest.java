package tc.oc.pgm.match;

import java.util.Optional;
import java.util.Random;

import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import org.bukkit.event.Listener;
import tc.oc.api.docs.virtual.MatchDoc;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.exception.ExceptionHandler;
import tc.oc.commons.core.inject.ChildInjectorFactory;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.inject.Manifest;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.random.Entropy;
import tc.oc.commons.core.random.MutableEntropy;
import tc.oc.commons.core.scheduler.SchedulerBackend;
import tc.oc.pgm.api.MatchDocument;
import tc.oc.pgm.countdowns.CountdownContext;
import tc.oc.pgm.countdowns.SingleCountdownContext;
import tc.oc.pgm.features.MatchFeatureContext;
import tc.oc.pgm.match.inject.ForMatch;
import tc.oc.pgm.match.inject.ForRunningMatch;
import tc.oc.pgm.match.inject.MatchBinders;
import tc.oc.pgm.match.inject.MatchScoped;
import tc.oc.pgm.module.MatchModulesManifest;
import tc.oc.pgm.time.TickClock;
import tc.oc.pgm.time.WorldTickClock;
import tc.oc.pgm.utils.WorldTickRandom;

/**
 * Configure things related to {@link Match}es
 *
 * @see MatchModulesManifest
 */
public class MatchManifest extends HybridManifest implements MatchBinders {
    @Override
    protected void configure() {
        // Make @MatchScoped work
        install(new MatchInjectionScope().new Manifest());

        // @MatchScoped stuff
        installIn(MatchScoped.class, new Manifest() {
            @Override protected void configure() {
                bind(Match.class).to(MatchImpl.class);
                bind(MatchModuleContext.class);
                bind(MatchFeatureContext.class);
                bind(WorldTickClock.class);
                bind(WorldTickRandom.class);
                bind(MatchDocument.class);
                bind(MatchAudiences.class);
                bind(MatchScheduler.class);
                bind(MatchRealtimeScheduler.class);
                bind(MatchExecutor.class);

                bind(Key.get(Entropy.class, ForMatch.class))
                    .to(MutableEntropy.class);

                bind(Key.get(Random.class, ForMatch.class))
                    .to(Random.class);

                bind(Key.get(SingleCountdownContext.class, ForMatch.class))
                    .to(SingleCountdownContext.class);
            }
        });

        // Tourney needs this
        expose(Match.class);

        // Aliases
        bind(MatchPlayerFinder.class).to(Match.class);
        bind(Audience.class).annotatedWith(ForMatch.class).to(Match.class);
        bind(TickClock.class).to(WorldTickClock.class);
        bind(MatchDoc.class).to(MatchDocument.class);

        bind(Key.get(CountdownContext.class, ForMatch.class))
            .to(Key.get(SingleCountdownContext.class, ForMatch.class));

        // Listeners to be registered at match load time
        Multibinder.newSetBinder(binder(), MatchListenerMeta.class);
        Multibinder.newSetBinder(binder(), Key.get(Listener.class, ForMatch.class));
        Multibinder.newSetBinder(binder(), new Key<Optional<? extends Listener>>(ForMatch.class){});

        matchListener(Key.get(CountdownContext.class, ForMatch.class));
        matchListener(MatchRealtimeScheduler.class);

        bind(new TypeLiteral<ChildInjectorFactory<MatchUserContext>>(){});

        installPlayerModule(new MatchPlayerFacetManifest());
    }

    @Provides @MatchScoped @ForRunningMatch
    MatchScheduler runningMatchScheduler(Loggers loggers, SchedulerBackend backend, ExceptionHandler exceptionHandler, Match match) {
        return new MatchScheduler(MatchScope.RUNNING, loggers, backend, exceptionHandler, match);
    }
}
