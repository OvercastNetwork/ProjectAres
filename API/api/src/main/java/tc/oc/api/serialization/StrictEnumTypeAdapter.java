package tc.oc.api.serialization;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Enum serializer that throws an exception when deserializing an unrecognized value.
 * (Gson's built-in adapter silently converts it to null)
 *
 * NOTE: Does not handle @SerializedName
 */
public class StrictEnumTypeAdapter<T extends Enum<T>> extends NullableTypeAdapter<T> {

    private final Class<T> type;

    public StrictEnumTypeAdapter(Class<T> type) {
        this.type = type;
    }

    @Override
    protected void writeNonNull(JsonWriter out, T value) throws IOException {
        out.value(value.name());
    }

    @Override
    protected T readNonNull(JsonReader in) throws IOException {
        final String value = in.nextString();
        try {
            return Enum.valueOf(type, value);
        } catch(IllegalArgumentException e) {
            throw new IOException("Unrecognized value '" + value + "' for enum " + type.getName(), e);
        }
    }

    public static class Factory implements TypeAdapterFactory {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if(Enum.class.isAssignableFrom(type.getRawType())) {
                return (TypeAdapter<T>) new StrictEnumTypeAdapter(type.getRawType());
            }
            return null;
        }
    }
}
