package tc.oc.pgm.features;

import java.util.Optional;
import javax.annotation.Nullable;

import tc.oc.commons.core.inspect.Inspectable;

/**
 * Methods common to {@link FeatureDefinition} and {@link FeatureReference}.
 *
 * When any of these methods are called on a reference, they will be dispatched
 * to the proxy object rather than the definition.
 */
public interface FeatureBase extends Inspectable {

    Class<? extends FeatureDefinition> getFeatureType();

    String getFeatureName();

    boolean isDefined();

    void assertDefined() throws IllegalStateException;

    @Nullable Class<? extends FeatureDefinition> getDefinitionType();

    default Class<? extends FeatureDefinition> needDefinitionType() {
        assertDefined();
        return getDefinitionType();
    }

    FeatureDefinition getDefinition() throws IllegalStateException;

    Optional<FeatureDefinition> tryDefinition();
}
