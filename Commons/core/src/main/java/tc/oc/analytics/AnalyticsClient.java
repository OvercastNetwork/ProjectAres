package tc.oc.analytics;

import tc.oc.minecraft.api.event.Activatable;

public interface AnalyticsClient extends Activatable {

    void count(String metric, int quantity);

    void measure(String metric, double value);

    void sample(String metric, double value);

    void event(Event event);
}
