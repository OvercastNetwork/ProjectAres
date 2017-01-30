package tc.oc.analytics;

import java.util.Set;
import javax.inject.Singleton;

import com.google.common.collect.ImmutableSet;
import tc.oc.commons.core.util.Threadable;
import tc.oc.commons.core.util.ThrowingRunnable;
import tc.oc.commons.core.util.ThrowingSupplier;

@Singleton
public class DynamicTagger implements Tagger {

    private final Threadable<ImmutableSet<Tag>> current = new Threadable<>(ImmutableSet::of);

    @Override
    public ImmutableSet<Tag> tags() {
        return current.need();
    }

    public <E extends Throwable> void withTags(Set<Tag> tags, ThrowingRunnable<E> block) throws E {
        current.let(
            ImmutableSet.<Tag>builder()
                .addAll(current.need())
                .addAll(tags)
                .build(),
            block
        );
    }

    public <U, E extends Throwable> U withTags(Set<Tag> tags, ThrowingSupplier<U, E> block) throws E {
        return current.let(
            ImmutableSet.<Tag>builder()
                .addAll(current.need())
                .addAll(tags)
                .build(),
            block
        );
    }

    public <E extends Throwable> void withTag(Tag tag, ThrowingRunnable<E> block) throws E {
        withTags(ImmutableSet.of(tag), block);
    }

    public <U, E extends Throwable> U withTag(Tag tag, ThrowingSupplier<U, E> block) throws E {
        return withTags(ImmutableSet.of(tag), block);
    }

    public <E extends Throwable> void withTag(String name, String value, ThrowingRunnable<E> block) throws E {
        withTag(Tag.of(name, value), block);
    }

    public <U, E extends Throwable> U withTag(String name, String value, ThrowingSupplier<U, E> block) throws E {
        return withTag(Tag.of(name, value), block);
    }
}
