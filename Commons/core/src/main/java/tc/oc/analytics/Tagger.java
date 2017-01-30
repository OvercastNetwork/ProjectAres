package tc.oc.analytics;

import com.google.common.collect.ImmutableSet;

public interface Tagger {
    ImmutableSet<Tag> tags();
}
