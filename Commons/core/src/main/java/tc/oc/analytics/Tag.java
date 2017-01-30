package tc.oc.analytics;

import java.util.Objects;

import tc.oc.commons.core.util.Utils;

public final class Tag {

    private final String name, value;

    private Tag(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public static Tag of(String name, String value) {
        return new Tag(name, value);
    }

    public String name() {
        return name;
    }

    public String value() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }

    @Override
    public boolean equals(Object obj) {
        return Utils.equals(Tag.class, this, obj, that ->
            this.name().equals(that.name()) &&
            this.value().equals(that.value())
        );
    }
}
