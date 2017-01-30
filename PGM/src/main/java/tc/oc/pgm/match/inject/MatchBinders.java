package tc.oc.pgm.match.inject;

import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import org.bukkit.event.Listener;
import tc.oc.commons.core.inject.Binders;
import tc.oc.commons.core.inject.ChildConfigurator;
import tc.oc.commons.core.inject.Keys;
import tc.oc.pgm.match.MatchListenerMeta;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.MatchUserContext;

public interface MatchBinders extends Binders {

    default void matchListener(Class<? extends Listener> type) {
        matchListener(type, null);
    }

    default <T extends Listener> void matchListener(Key<T> key) {
        matchListener(key, null);
    }

    default void matchListener(Class<? extends Listener> type, @Nullable MatchScope scope) {
        matchListener(Key.get(type), scope);
    }

    default <T extends Listener> void matchListener(Key<T> key, @Nullable MatchScope scope) {
        inSet(MatchListenerMeta.class).addBinding().toInstance(
            new MatchListenerMeta((Class<T>) key.getTypeLiteral().getRawType(), scope)
        );

        inSet(Key.get(Listener.class, ForMatch.class))
            .addBinding().to(key);
    }

    default void matchOptionalListener(Class<? extends Listener> type) {
        matchOptionalListener(Key.get(type), null);
    }

    default <T extends Listener> void matchOptionalListener(Key<T> key, @Nullable MatchScope scope) {
        inSet(MatchListenerMeta.class).addBinding().toInstance(
            new MatchListenerMeta((Class<T>) key.getTypeLiteral().getRawType(), scope)
        );

        final Key<Optional<T>> optionalKey = Keys.optional(key);
        inSet(Key.get(new TypeLiteral<Optional<? extends Listener>>(){}, ForMatch.class))
            .addBinding().to(optionalKey);
    }
    /**
     * Install modules into the private configuration of *every* {@link MatchUserContext}
     */
    default void installUserModule(Module... modules) {
        final ChildConfigurator<MatchUserContext> configurator = new ChildConfigurator<>(forwardedBinder(), MatchUserContext.class);
        Stream.of(modules).forEach(configurator::install);
    }

    /**
     * Install modules into the private configuration of *every* {@link MatchPlayer}
     */
    default void installPlayerModule(Module... modules) {
        final ChildConfigurator<MatchPlayer> configurator = new ChildConfigurator<>(forwardedBinder(), MatchPlayer.class);
        Stream.of(modules).forEach(configurator::install);
    }
}
