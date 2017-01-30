package tc.oc.api.serialization;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import javax.inject.Singleton;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import com.google.inject.Provides;
import tc.oc.commons.core.inject.Manifest;

public class SerializationManifest extends Manifest {

    private static final String ISO8601_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssX";

    @Override
    protected void configure() {
        install(new TypeAdaptersManifest());
    }

    @Provides
    GsonBuilder gsonBuilder(Set<TypeAdapterFactory> factories, Map<Type, Object> adapters, Map<Class, Object> hiearchyAdapters) {
        GsonBuilder builder = new GsonBuilder()
            .setDateFormat(ISO8601_DATE_FORMAT)
            .serializeSpecialFloatingPointValues() // Infinity and NaN
            .serializeNulls(); // Needed so we can clear fields in PartialModel document updates

        factories.forEach(builder::registerTypeAdapterFactory);
        adapters.forEach(builder::registerTypeAdapter);
        hiearchyAdapters.forEach(builder::registerTypeHierarchyAdapter);

        return builder;
    }

    @Provides @Singleton
    Gson gson(GsonBuilder builder) {
        return builder.create();
    }

    @Provides @Singleton @Pretty
    Gson prettyGson(GsonBuilder builder) {
        return builder.setPrettyPrinting().create();
    }
}
