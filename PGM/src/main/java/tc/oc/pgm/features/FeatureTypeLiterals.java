package tc.oc.pgm.features;

import com.google.inject.TypeLiteral;
import tc.oc.commons.core.reflect.ResolvableType;
import tc.oc.commons.core.reflect.TypeArgument;

public interface FeatureTypeLiterals {

    default <T extends FeatureDefinition> TypeLiteral<Feature<T>> Feature(TypeLiteral<T> type) {
        return new ResolvableType<Feature<T>>(){}.with(new TypeArgument<T>(type){});
    }

    default <T extends FeatureDefinition> TypeLiteral<Feature<T>> Feature(Class<T> type) {
        return Feature(TypeLiteral.get(type));
    }

    default <T extends FeatureDefinition> TypeLiteral<FeatureParser<T>> FeatureParser(TypeLiteral<T> type) {
        return new ResolvableType<FeatureParser<T>>(){}.with(new TypeArgument<T>(type){});
    }

    default <T extends FeatureDefinition> TypeLiteral<FeatureParser<T>> FeatureParser(Class<T> type) {
        return FeatureParser(TypeLiteral.get(type));
    }

    default <T extends FeatureDefinition> TypeLiteral<FeatureDefinitionParser<T>> FeatureDefinitionParser(TypeLiteral<T> type) {
        return new ResolvableType<FeatureDefinitionParser<T>>(){}.with(new TypeArgument<T>(type){});
    }

    default <T extends FeatureDefinition> TypeLiteral<FeatureDefinitionParser<T>> FeatureDefinitionParser(Class<T> type) {
        return FeatureDefinitionParser(TypeLiteral.get(type));
    }

    default <T extends FeatureDefinition> TypeLiteral<ReflectiveFeatureParser<T>> ReflectiveFeatureParser(TypeLiteral<T> type) {
        return new ResolvableType<ReflectiveFeatureParser<T>>(){}.with(new TypeArgument<T>(type){});
    }

    default <T extends FeatureDefinition> TypeLiteral<ReflectiveFeatureParser<T>> ReflectiveFeatureParser(Class<T> type) {
        return ReflectiveFeatureParser(TypeLiteral.get(type));
    }
}
