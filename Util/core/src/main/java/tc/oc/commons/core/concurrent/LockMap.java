package tc.oc.commons.core.concurrent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.inject.Inject;
import javax.inject.Provider;

import tc.oc.commons.core.util.CheckedCloseable;

public class LockMap<K, L extends Lock> {

    private final ConcurrentMap<K, L> locks = new ConcurrentHashMap<>();
    private final Provider<L> lockProvider;
    private final boolean reuseLocks;

    @Inject public LockMap(Provider<L> lockProvider, boolean reuseLocks) {
        this.lockProvider = lockProvider;
        this.reuseLocks = reuseLocks;
    }

    public CheckedCloseable lock(K key) {
        final L lock = locks.computeIfAbsent(key, k -> lockProvider.get());
        lock.lock();
        return () -> {
            if(!reuseLocks) locks.remove(key);
            lock.unlock();
        };
    }

    public static <K> LockMap<K, ReentrantLock> newReentrantLockMap(boolean reuseLocks) {
        return new LockMap<>(ReentrantLock::new, reuseLocks);
    }
}
