package tc.oc.commons.core.concurrent;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Executor;

/**
 * Non-concurrent Executor that ensures tasks run in the order they are submitted,
 * and not more than one at a time. If a task submits another task, the latter
 * will not actually run until the former returns.
 *
 * This is NOT thread-safe, all tasks must be submitted from the same thread.
 */
public class SerializingExecutor implements Executor {

    private final Deque<Runnable> queue = new ArrayDeque<>();
    private boolean processing;

    @Override
    public void execute(Runnable command) {
        queue.addLast(command);
        process();
    }

    private void process() {
        if(!processing) {
            processing = true;
            try {
                Runnable runnable;
                while((runnable = queue.pollFirst()) != null) {
                    runnable.run();
                }
            } finally {
                processing = false;
            }
        }
    }
}
