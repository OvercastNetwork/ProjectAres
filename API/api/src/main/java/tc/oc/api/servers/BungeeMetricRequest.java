package tc.oc.api.servers;

public class BungeeMetricRequest {
    public String ip;
    public Type type;

    public BungeeMetricRequest(String ip, Type type) {
        this.ip = ip;
        this.type = type;
    }

    public enum Type {
        PING,
        LOGIN,
    }
}
