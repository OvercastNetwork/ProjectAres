package tc.oc.analytics;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;

/**
 * Number of occurances of some momentary event,
 * or a quantity associated with those events.
 */
public class Count extends Metric {

    @Inject Count(@Assisted String name) {
        super(name);
    }

    public void increment(int quantity) {
        driver.count(name(), quantity);
    }

    public void decrement(int quantity) {
        increment(-quantity);
    }

    public void increment() {
        increment(1);
    }

    public void decrement() {
        decrement(1);
    }
}
