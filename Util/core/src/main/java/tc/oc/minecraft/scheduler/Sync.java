package tc.oc.minecraft.scheduler;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.inject.Qualifier;

import com.google.inject.BindingAnnotation;
import tc.oc.commons.core.reflect.AnnotationBase;

/**
 * Binding qualifier for executors that are synchronized with the "main"
 * thread of the server, if there is one.
 *
 * @see MinecraftExecutorManifest
 */
@Qualifier @BindingAnnotation @Retention(RetentionPolicy.RUNTIME)
public @interface Sync {

    /**
     * If true, tasks are always queued, even if submitted from the main thread.
     *
     * If false, tasks that are submitted from the main thread will run immediately.
     */
    boolean defer() default false;

    Sync immediate = new SyncImpl() {
        @Override public boolean defer() {
            return false;
        }
    };

    Sync deferred = new SyncImpl() {
        @Override public boolean defer() {
            return true;
        }
    };
}

abstract class SyncImpl extends AnnotationBase implements Sync {}
