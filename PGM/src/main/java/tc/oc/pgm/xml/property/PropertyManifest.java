package tc.oc.pgm.xml.property;

import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import tc.oc.commons.core.inject.KeyedManifest;
import tc.oc.commons.core.reflect.ResolvableType;
import tc.oc.commons.core.reflect.TypeArgument;
import tc.oc.commons.core.reflect.TypeParameter;
import tc.oc.commons.core.reflect.Types;

import static com.google.common.base.Preconditions.checkNotNull;

public class PropertyManifest<T, B extends PropertyBuilder<T, B>> extends KeyedManifest {

    private final TypeLiteral<T> type;
    private final TypeArgument<T> typeArg;
    private final TypeLiteral<B> builderType;
    private final TypeArgument<B> builderTypeArg;
    private final boolean defaultForType;

    public PropertyManifest(Class<T> type) {
        this(TypeLiteral.get(type));
    }

    public PropertyManifest(Class<T> type, Class<B> builderType) {
        this(type, builderType, true);
    }

    public PropertyManifest(Class<T> type, Class<B> builderType, boolean defaultForType) {
        this(TypeLiteral.get(type), TypeLiteral.get(builderType), defaultForType);
    }

    public PropertyManifest(TypeLiteral<T> type) {
        this(type, (TypeLiteral<B>) new ResolvableType<PropertyBuilder<T, ?>>(){}.where(new TypeParameter<T>(){}, type));
    }

    public PropertyManifest(TypeLiteral<T> type, TypeLiteral<B> builderType) {
        this(type, builderType, true);
    }

    public PropertyManifest(TypeLiteral<T> type, TypeLiteral<B> builderType, boolean defaultForType) {
        this.type = Types.assertFullySpecified(checkNotNull(type));
        this.builderType = Types.assertFullySpecified(checkNotNull(builderType));
        this.typeArg = new TypeArgument<T>(this.type){};
        this.builderTypeArg = new TypeArgument<B>(this.builderType){};
        this.defaultForType = defaultForType;
    }

    @Override
    protected Object manifestKey() {
        return type;
    }

    @Override
    protected void configure() {
        final TypeLiteral<PropertyBuilderFactory<T, B>> factoryType = new ResolvableType<PropertyBuilderFactory<T, B>>(){}.with(typeArg, builderTypeArg);
        install(new FactoryModuleBuilder().build(factoryType));

        if(defaultForType) {
            final TypeLiteral<PropertyBuilderFactory<T, ?>> baseFactoryType = new ResolvableType<PropertyBuilderFactory<T, ?>>(){}.with(typeArg);
            bind(baseFactoryType).to(factoryType);
        }
    }
}
