package tc.oc.commons.core.concurrent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.AbstractListeningExecutorService;
import tc.oc.commons.core.util.Traceables;

public class ExecutorServiceDecorator extends AbstractListeningExecutorService {

    private final Executor executor;
    private final Lock lock = new ReentrantLock();
    private final Condition termination = lock.newCondition();
    private boolean shutdown;
    private Map<Object, Runnable> pending = new HashMap<>();
    private int runningTasks;

    public ExecutorServiceDecorator(Executor executor) {
        this.executor = executor;
    }

    @Override
    public void execute(Runnable command) {
        final Object key = queueTask(command);
        executor.execute(Traceables.wrap(command, () -> {
            if(startTask(key)) {
                try {
                    command.run();
                } finally {
                    endTask();
                }
            }
        }));
    }

    @Override
    public boolean isShutdown() {
        lock.lock();
        try {
            return shutdown;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void shutdown() {
        lock.lock();
        try {
            shutdown = true;
            pending.clear();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<Runnable> shutdownNow() {
        lock.lock();
        try {
            shutdown = true;
            final List<Runnable> list = ImmutableList.copyOf(pending.values());
            pending.clear();
            return list;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isTerminated() {
        lock.lock();
        try {
            return shutdown && runningTasks == 0;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        lock.lock();
        try {
            for (;;) {
                if (isTerminated()) {
                    return true;
                } else if (nanos <= 0) {
                    return false;
                } else {
                    nanos = termination.awaitNanos(nanos);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private Object queueTask(Runnable task) {
        lock.lock();
        try {
            if(isShutdown()) {
                throw new RejectedExecutionException("Executor is already shut down");
            }
            final Object key = new Object();
            pending.put(key, task);
            return key;
        } finally {
            lock.unlock();
        }
    }

    private boolean startTask(Object key) {
        lock.lock();
        try {
            if(!shutdown && pending.remove(key) != null) {
                runningTasks++;
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    private void endTask() {
        lock.lock();
        try {
            runningTasks--;
            if(isTerminated()) {
                termination.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }
}
