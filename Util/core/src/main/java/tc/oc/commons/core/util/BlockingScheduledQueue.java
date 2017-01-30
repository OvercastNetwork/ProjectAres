package tc.oc.commons.core.util;

import java.time.Duration;
import java.time.Instant;
import java.util.PriorityQueue;
import java.util.function.Function;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkState;

/**
 * A queue of elements that are stamped with the earliest time they can be
 * removed from the queue. The {@link #get} methods block until an element
 * is removable. The queue is thread-safe for any number of readers and
 * writers. An element can only be removed once, so multiple readers will
 * receive different elements.
 *
 * The timestamp for each element can be provided directly to the {@link #add}
 * method, or derived from a "chronarator" function passed to the constructor.
 */
public class BlockingScheduledQueue<T> {

    private static class Entry<T> implements Comparable<Entry> {
        final T element;
        final long time;

        private Entry(T element, Instant time) {
            this.element = element;
            this.time = time.toEpochMilli();
        }

        @Override
        public int compareTo(Entry o) {
            return Long.compare(time, o.time);
        }
    }

    private final PriorityQueue<Entry<T>> queue = new PriorityQueue<>();
    private final @Nullable Function<T, Instant> chronarator;

    public BlockingScheduledQueue() {
        this(null);
    }

    public BlockingScheduledQueue(@Nullable Function<T, Instant> chronarator) {
        this.chronarator = chronarator;
    }

    public void add(T element) {
        checkState(chronarator != null);
        add(element, chronarator.apply(element));
    }

    public void add(T element, Instant time) {
        synchronized(this) {
            queue.add(new Entry<>(element, time));
            notify();
        }
    }

    public T get() throws InterruptedException {
        for(;;) {
            synchronized(this) {
                final long now = Instant.now().toEpochMilli();
                final Entry<T> entry = queue.peek();
                if(entry == null) {
                    wait();
                } else {
                    final long untilNext = entry.time - now;
                    if(untilNext <= 0) return queue.remove().element;
                    wait(untilNext);
                }
            }
        }
    }

    public T get(Duration timeout) throws InterruptedException {
        final long limit = Instant.now().plus(timeout).toEpochMilli();
        for(;;) {
            synchronized(this) {
                final long now = Instant.now().toEpochMilli();
                final Entry<T> entry = queue.peek();
                long wait = limit - now;

                if(entry != null) {
                    final long untilNext = entry.time - now;
                    if(untilNext <= 0) return queue.remove().element;
                    wait = Math.min(wait, untilNext);
                }

                if(wait <= 0) throw new InterruptedException();
                wait(wait);
            }
        }
    }
}
