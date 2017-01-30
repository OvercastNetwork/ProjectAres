package tc.oc.analytics;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;

/**
 * Number associated with distinct samples (generates a histogram)
 */
public class Distribution extends Metric {

    @Inject Distribution(@Assisted String name) {
        super(name);
    }

    public void sample(double value) {
        driver.sample(name(), value);
    }
}
