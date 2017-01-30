package tc.oc.analytics;

public interface Event {
    enum Level {
        SUCCESS, INFO, WARNING, ERROR
    }

    Level level();
    String key();
    String title();
    String body();

    default Event withBody(String body) {
        return new EventImpl(level(), key(), title(), body);
    }

    static Event of(Level level, String key, String title, String body) {
        return new EventImpl(level, key, title, body);
    }

    static Event of(Level level, String key, String title) {
        return new EventImpl(level, key, title, "");
    }

    static Event success(String key, String title) {
        return of(Level.SUCCESS, key, title);
    }

    static Event info(String key, String title) {
        return of(Level.INFO, key, title);
    }

    static Event warning(String key, String title) {
        return of(Level.WARNING, key, title);
    }

    static Event error(String key, String title) {
        return of(Level.ERROR, key, title);
    }
}

class EventImpl implements Event {
    private final Level level;
    private final String key, title, body;

    EventImpl(Level level, String key, String title, String body) {
        this.level = level;
        this.key = key;
        this.title = title;
        this.body = body;
    }

    @Override
    public Level level() {
        return level;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public String body() {
        return body;
    }
}
