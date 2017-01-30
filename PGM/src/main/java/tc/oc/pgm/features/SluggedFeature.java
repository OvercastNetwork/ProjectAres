package tc.oc.pgm.features;

public interface SluggedFeature<T extends SluggedFeatureDefinition> extends Feature<T> {
    String slug();
}
