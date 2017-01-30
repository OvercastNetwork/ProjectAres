package tc.oc.pgm.features;

public interface SluggedFeatureDefinition extends FeatureDefinition {
    /**
     * Return a fairly distinct identifier for this feature, suitable for record-keeping.
     *
     * This name should be developer-readable, and relatively stable across changes to the map.
     *
     * Implementors should TRY to generate a unique string from the properties of the feature,
     * but should NOT add meaningless information to the string in order to accomplish this.
     */
    default String defaultSlug() {
        return "--" + getFeatureName();
    }

    default String slugify(String text) {
        return text.toLowerCase().replaceAll("\\s+", "-");
    }
}
