package tc.oc.pgm.module;

import javax.annotation.Nullable;

/**
 * Thrown for map-related problems that cannot be detected until match load time
 */
public class ModuleLoadException extends Exception {

    private @Nullable Class<?> module;

    public ModuleLoadException(@Nullable Class<?> module, String message, Throwable cause) {
        super(message, cause);
        this.module = module;
    }

    public ModuleLoadException(@Nullable Class<?> module, String message) {
        this(module, message, null);
    }

    public ModuleLoadException(@Nullable Class<?> module) {
        this(module, null);
    }

    public ModuleLoadException(String message, Throwable cause) {
        this(null, message, cause);
    }

    public ModuleLoadException(String message) {
        this(message, null);
    }

    public ModuleLoadException() {
        this((Class<?>) null);
    }

    public void offerModule(Class<?> module) {
        if(this.module == null) this.module = module;
    }

    public @Nullable Class<?> module() {
        return module;
    }
}
