package tc.oc.analytics;

public interface MetricFactory {

    Count count(String name);

    Gauge gauge(String name);

    Distribution distribution(String name);
}
