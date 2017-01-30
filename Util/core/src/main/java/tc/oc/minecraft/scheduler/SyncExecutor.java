package tc.oc.minecraft.scheduler;

import com.google.common.util.concurrent.ListeningExecutorService;
import tc.oc.commons.core.concurrent.Flexecutor;

/**
 * Queue things to run on the main thread.
 *
 * @deprecated Use the {@link Sync} qualifier as explained in {@link MinecraftExecutorManifest}
 */
@Deprecated
public interface SyncExecutor extends ListeningExecutorService, Flexecutor {}
