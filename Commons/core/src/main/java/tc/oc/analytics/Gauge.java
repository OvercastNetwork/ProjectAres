package tc.oc.analytics;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;

/**
 * Number that varies over time, measured at arbitrary times.
 */
public class Gauge extends Metric {

    @Inject Gauge(@Assisted String name) {
        super(name);
    }

    public void measure(double value) {
        driver.measure(name(), value);
    }
}
