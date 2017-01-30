package tc.oc.commons.core.concurrent;

import java.util.concurrent.locks.Lock;
import javax.annotation.Nullable;

/**
 * Usage:
 *
 * <pre>
 *     try(Locker locker = Locker.lock(someLock)) {
 *         ...
 *     }
 *
 *     try(Locker locker = Locker.lockInterruptibly(someLock)) {
 *         ...
 *     } catch(InterruptedException e) {
 *         ...
 *     }
 *
 *     try(Locker locker = Locker.tryLock(someLock)) {
 *         if(locker != null) {
 *             ...
 *         }
 *     }
 * </pre>
 */
public class Locker implements AutoCloseable {
    private final Lock lock;

    private Locker(Lock lock) {
        this.lock = lock;
    }

    @Override
    public void close() {
        lock.unlock();
    }

    public static Locker lock(Lock lock) {
        lock.lock();
        return new Locker(lock);
    }

    public static Locker lockInterruptibly(Lock lock) throws InterruptedException {
        lock.lockInterruptibly();
        return new Locker(lock);
    }

    public static @Nullable Locker tryLock(Lock lock) {
        return lock.tryLock() ? new Locker(lock) : null;
    }
}
