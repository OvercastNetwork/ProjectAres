package tc.oc.api.serialization;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;

public class GsonBinder {

    private final Multibinder<TypeAdapterFactory> factories;
    private final MapBinder<Type, Object> adapters;
    private final MapBinder<Class, Object> hiearchyAdapters;

    public GsonBinder(Binder binder) {
        factories = Multibinder.newSetBinder(binder, TypeAdapterFactory.class);
        adapters = MapBinder.newMapBinder(binder, Type.class, Object.class);
        hiearchyAdapters = MapBinder.newMapBinder(binder, Class.class, Object.class);
    }

    public LinkedBindingBuilder<TypeAdapterFactory> bindFactory() {
        return factories.addBinding();
    }

    public <T> LinkedBindingBuilder<TypeAdapter<T>> bindAdapter(Class<T> type) {
        return (LinkedBindingBuilder) adapters.addBinding(type);
    }

    public <T> LinkedBindingBuilder<TypeAdapter<T>> bindAdapter(TypeLiteral<T> type) {
        return (LinkedBindingBuilder) adapters.addBinding(type.getType());
    }

    public <T> LinkedBindingBuilder<JsonSerializer<? super T>> bindSerializer(Class<T> type) {
        return (LinkedBindingBuilder) adapters.addBinding(type);
    }

    public <T> LinkedBindingBuilder<JsonSerializer<? super T>> bindSerializer(TypeLiteral<T> type) {
        return (LinkedBindingBuilder) adapters.addBinding(type.getType());
    }

    public <T> LinkedBindingBuilder<JsonDeserializer<? extends T>> bindDeserializer(Class<T> type) {
        return (LinkedBindingBuilder) adapters.addBinding(type);
    }

    public <T> LinkedBindingBuilder<JsonDeserializer<? extends T>> bindDeserializer(TypeLiteral<T> type) {
        return (LinkedBindingBuilder) adapters.addBinding(type.getType());
    }

    public <T> LinkedBindingBuilder<TypeAdapter<T>> bindHiearchyAdapter(Class<T> type) {
        return (LinkedBindingBuilder) hiearchyAdapters.addBinding(type);
    }

    public <T> LinkedBindingBuilder<JsonSerializer<? super T>> bindHiearchySerializer(Class<T> type) {
        return (LinkedBindingBuilder) hiearchyAdapters.addBinding(type);
    }

    public <T> LinkedBindingBuilder<JsonDeserializer<? extends T>> bindHiearchyDeserializer(Class<T> type) {
        return (LinkedBindingBuilder) hiearchyAdapters.addBinding(type);
    }
}
