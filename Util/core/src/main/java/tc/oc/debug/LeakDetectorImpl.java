package tc.oc.debug;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import java.time.Duration;
import java.time.Instant;

import tc.oc.commons.core.inspect.Inspectable;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.util.TimeUtils;

/**
 * Logs errors if objects are not garbage collected within a certain time.
 * Just call one of the {@link #expectRelease} methods and specify by when
 * the object should be released.
 */
@Singleton
public class LeakDetectorImpl extends AbstractExecutionThreadService implements LeakDetector {

    private final Logger logger;
    private final LeakDetectorConfig config;

    // The JVM will add references to this queue when they are garbage collected
    private final ReferenceQueue queue = new ReferenceQueue<>();

    // References are removed from this queue as they expire
    private final PriorityQueue<Reference> deadlines = new PriorityQueue<>(Comparator.comparingLong(reference -> reference.deadlineNanos));

    private volatile Thread thread;

    @Inject LeakDetectorImpl(Loggers logger, LeakDetectorConfig config) {
        this.logger = logger.get(LeakDetectorImpl.class);
        this.config = config;
    }

    @Override
    public void expectRelease(Object obj, Instant deadline, boolean forceCollection) {
        expectRelease(obj, TimeUtils.durationUntil(deadline), forceCollection);
    }

    @Override
    public void expectRelease(Object obj, Duration within, boolean forceCollection) {
        if(!config.enabled() || obj == null) return;

        if(state() == State.NEW) {
            startAsync();
            awaitRunning();
        }

        final Reference reference;
        synchronized(deadlines) {
            reference = new Reference(obj, within, forceCollection);
            deadlines.add(reference);
        }

        logger.fine(() -> "Waiting " + within + " for release of " + reference.inspect());

        if(thread != null) thread.interrupt();
    }

    @Override
    protected void triggerShutdown() {
        if(thread != null) thread.interrupt();
    }

    @Override
    protected void run() throws Exception {
        logger.fine("Starting");
        thread = Thread.currentThread();
        while(isRunning()) {
            try {
                final Reference expiring, released;
                synchronized(deadlines) {
                    expiring = deadlines.peek();
                }

                if(expiring == null) {
                    released = (Reference) queue.remove();
                } else {
                    final long timeout = expiring.millisUntilDeadline();
                    released = (Reference) (timeout > 0 ? queue.remove(timeout)
                                                        : queue.poll());
                }

                if(released != null) {
                    synchronized(deadlines) {
                        deadlines.remove(released);
                    }
                    logger.fine(() -> "Released " + released.inspect());
                } else if(expiring != null && expiring.isExpired()) {
                    if(expiring.forceCollection && !expiring.triedCollection) {
                        synchronized(deadlines) {
                            for(Reference r : deadlines) {
                                if(r.forceCollection && r.isExpired()) r.triedCollection = true;
                            }
                        }
                        System.gc();
                    } else {
                        synchronized(deadlines) {
                            deadlines.remove(expiring);
                        }
                        logger.severe("Leaked " + expiring.inspect());
                    }
                }

            } catch(InterruptedException e) {
                // continue
            }
        }
        synchronized(deadlines) {
            deadlines.clear();
        }
        logger.fine("Stopping");
    }

    class Reference extends WeakReference {
        final String label;
        final long deadlineNanos;
        final boolean forceCollection;
        boolean triedCollection;

        Reference(Object referent, Duration within, boolean forceCollection) {
            super(referent, queue);
            this.deadlineNanos = System.nanoTime() + within.toNanos();
            this.forceCollection = forceCollection;
            this.label = referent.getClass().getName() + ":" + System.identityHashCode(referent);
        }

        long millisUntilDeadline() {
            return TimeUnit.NANOSECONDS.toMillis(Math.max(0, deadlineNanos - System.nanoTime()));
        }

        boolean isExpired() {
            return deadlineNanos <= System.nanoTime() && !isEnqueued() && this.get() != null;
        }

        String inspect() {
            String text = label;
            final Object obj = get();
            if(obj != null) {
                text += " :: ";
                if(obj instanceof Inspectable) {
                    try {
                        text += ((Inspectable) obj).identify();
                    } catch(Throwable ex) {
                        text += "[Inspectable#identify() threw " + ex.getClass() + "]";
                    }
                } else {
                    try {
                        text += obj.toString();
                    } catch(Throwable ex) {
                        text += "[Object#toString() threw " + ex.getClass() + "]";
                    }
                }
            }
            return text;
        }
    }
}
