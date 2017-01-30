package tc.oc.analytics;

import java.util.Iterator;

import com.google.common.collect.ImmutableSet;

public class TagSetBuilder {

    private final ImmutableSet.Builder<Tag> builder = ImmutableSet.builder();

    public ImmutableSet<Tag> build() {
        return builder.build();
    }

    public TagSetBuilder add(Tag tag) {
        builder.add(tag);
        return this;
    }

    public TagSetBuilder add(Tag... tags) {
        builder.add(tags);
        return this;
    }

    public TagSetBuilder addAll(Iterable<? extends Tag> tags) {
        builder.addAll(tags);
        return this;
    }

    public TagSetBuilder addAll(Iterator<? extends Tag> tags) {
        builder.addAll(tags);
        return this;
    }

    public TagSetBuilder add(String name, String value) {
        return add(Tag.of(name, value));
    }

    public TagSetBuilder addAll(String name, Iterable<String> values) {
        values.forEach(value -> add(name, value));
        return this;
    }
}
