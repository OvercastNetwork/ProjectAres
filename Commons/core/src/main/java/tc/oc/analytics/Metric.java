package tc.oc.analytics;

import javax.inject.Inject;

public abstract class Metric {

    @Inject protected AnalyticsClient driver;

    private final String name;

    Metric(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }
}
