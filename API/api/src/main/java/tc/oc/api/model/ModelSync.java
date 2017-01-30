package tc.oc.api.model;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.ExecutorService;
import javax.inject.Qualifier;

import com.google.inject.BindingAnnotation;

/**
 * An {@link ExecutorService} annotated with this runs tasks in sync
 * with all {@link ModelStore}s, so all data is constant during a
 * single execution.
 *
 * In Bukkit/Bungee environments, the executor simply runs everything
 * on the main thread.
 */
@Qualifier
@BindingAnnotation
@Retention(RetentionPolicy.RUNTIME)
public @interface ModelSync {}
