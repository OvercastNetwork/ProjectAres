package tc.oc.analytics;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Stage;

@Singleton
public class StageTagger implements Tagger {

    private final ImmutableSet<Tag> tags;

    @Inject StageTagger(Stage stage) {
        this.tags = ImmutableSet.of(Tag.of("environment", stage.name().toLowerCase()));
    }

    @Override
    public ImmutableSet<Tag> tags() {
        return tags;
    }
}
