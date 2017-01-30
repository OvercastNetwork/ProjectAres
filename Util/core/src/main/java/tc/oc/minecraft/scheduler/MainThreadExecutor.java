package tc.oc.minecraft.scheduler;

import com.google.common.util.concurrent.ListeningExecutorService;
import tc.oc.commons.core.concurrent.Flexecutor;

/**
 * Runs things on the main thread.
 *
 * @deprecated Use the {@link Sync} qualifier as explained in {@link MinecraftExecutorManifest}
 */
@Deprecated
public interface MainThreadExecutor extends Flexecutor, ListeningExecutorService {}
