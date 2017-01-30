package tc.oc.api.message;

public class MessageMeta<T extends Message> {

    private final Class<T> type;
    private final String name;

    public MessageMeta(Class<T> type, String name) {
        this.name = name;
        this.type = type;
    }

    public Class<T> type() {
        return type;
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
