package tc.oc.pgm.features;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import tc.oc.pgm.xml.Node;
import tc.oc.pgm.xml.validate.Validation;
import tc.oc.pgm.xml.validate.ValidationContext;

public interface FeatureValidationContext extends ValidationContext {

    /**
     * Register a validation to run on the given feature or reference in the post-parse phase.
     * The feature must be registered with this context, but does not need to be defined yet
     * i.e. may be a reference.
     *
     * The given source node will be the location reported with the error if the validation fails.
     * It should be the source of the validation, rather than the source of the feature itself.
     */
    <T extends FeatureDefinition> T validate(T feature, @Nullable Node source, Stream<Validation<? super T>> validations);

    default <T extends FeatureDefinition> T validate(T feature, Validation<? super T>... validations) {
        return validate(feature, null, validations);
    }

    default <T extends FeatureDefinition> T validate(T feature, @Nullable Node source, Validation<? super T>... validations) {
        return validate(feature, source, Arrays.asList(validations));
    }

    default <T extends FeatureDefinition> T validate(T feature, @Nullable Node source, Collection<Validation<? super T>> validations) {
        return validate(feature, source, validations.stream());
    }
}
