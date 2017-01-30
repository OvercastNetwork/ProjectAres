package tc.oc.pgm.match;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;

import com.google.common.base.Joiner;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import java.time.Duration;
import tc.oc.commons.core.exception.ExceptionHandler;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.reflect.Methods;
import tc.oc.commons.core.scheduler.Scheduler;
import tc.oc.commons.core.scheduler.SchedulerBackend;
import tc.oc.commons.core.scheduler.Task;
import tc.oc.commons.core.stream.Collectors;
import tc.oc.time.Time;
import tc.oc.commons.core.util.CacheUtils;
import tc.oc.commons.core.util.ThrowingRunnable;

/**
 * A scheduler that is active for the duration of a {@link Match}.
 */
public class MatchScheduler extends Scheduler {

    private final Match match;
    private final MatchScope scope;
    private final Map<Object, Set<Task>> tasksByInstance = new IdentityHashMap<>();

    @Inject MatchScheduler(Loggers loggers, SchedulerBackend backend, ExceptionHandler exceptionHandler, Match match) {
        this(MatchScope.LOADED, loggers, backend, exceptionHandler, match);
    }

    MatchScheduler(MatchScope scope, Loggers loggers, SchedulerBackend backend, ExceptionHandler exceptionHandler, Match match) {
        super(loggers, backend, exceptionHandler, false);
        this.match = match;
        this.scope = scope;
    }

    void registerRepeatables(final Object object) {
        tasksByInstance.computeIfAbsent(object, o -> repeatableMethodsByClass
            .getUnchecked(object.getClass())
            .stream()
            .filter(rm -> rm.scope == scope)
            .map(repeatable -> {
                MethodHandle handle = repeatable.handle.bindTo(object);
                if(handle.type().parameterCount() > 0) {
                    handle = handle.bindTo(match);
                }
                final MethodHandle finalHandle = handle;
                return register(repeatable.parameters, (ThrowingRunnable<?>) finalHandle::invokeExact, null);
            })
            .collect(Collectors.toImmutableSet())
        );
    }

    void unregisterRepeatables(final Object object) {
        final Set<Task> set = tasksByInstance.remove(object);
        if(set != null) {
            set.forEach(Task::cancel);
        }
    }

    private static class RepeatableMethod {
        final MethodHandle handle;
        final Task.Parameters parameters;
        final MatchScope scope;

        RepeatableMethod(MethodHandle handle, Task.Parameters parameters, MatchScope scope) {
            this.handle = handle;
            this.parameters = parameters;
            this.scope = scope;
        }
    }

    private static final LoadingCache<Class<?>, ImmutableSet<RepeatableMethod>> repeatableMethodsByClass = CacheUtils.newCache(cls -> {
        final ImmutableSet.Builder<RepeatableMethod> methods = ImmutableSet.builder();

        Methods.declaredMethodsInAncestors(cls).forEach(method -> {
            final Repeatable annotation = method.getAnnotation(Repeatable.class);
            if(annotation != null) {
                method.setAccessible(true);

                final Class<?>[] parameters = method.getParameterTypes();
                if(!(parameters.length == 0 || (parameters.length == 1 && Match.class.isAssignableFrom(parameters[0])))) {
                    throw new IllegalArgumentException(method.getName() + " does not have compatible parameter types (" + Joiner.on(",").join(parameters) + ")");
                }

                try {
                    methods.add(new RepeatableMethod(
                        MethodHandles.publicLookup().unreflect(method),
                        Task.Parameters.fromDuration(Duration.ZERO, Time.convertTo.duration(annotation.interval())),
                        annotation.scope()
                    ));
                } catch(IllegalAccessException e) {
                    throw new IllegalStateException("Failed to get handle for repeatable method " + method, e);
                }
            }
        });

        return methods.build();
    });
}
