package tc.oc.pgm.features;

import tc.oc.pgm.match.Match;
import tc.oc.pgm.module.ModuleLoadException;

public interface FeatureFactory<T extends Feature<?>> {
    /**
     * Called at most once per match to create the {@link T} instance.
     *
     * This is called lazily by {@link MatchFeatureContext#get(FeatureFactory)}.
     * To create the feature eagerly, call that method at match load time.
     */
    T createFeature(Match match) throws ModuleLoadException;
}
