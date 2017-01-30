package tc.oc.api.serialization;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class InstantTypeAdapter extends NullableTypeAdapter<Instant> {
    public static class Factory implements TypeAdapterFactory {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if(Instant.class == type.getRawType()) {
                return (TypeAdapter<T>) new InstantTypeAdapter(gson.getAdapter(Date.class));
            }
            return null;
        }
    }

    private final TypeAdapter<Date> dateAdapter;

    public InstantTypeAdapter(TypeAdapter<Date> dateAdapter) {
        this.dateAdapter = dateAdapter;
    }

    @Override
    protected void writeNonNull(JsonWriter out, Instant value) throws IOException {
        dateAdapter.write(out, Date.from(value));
    }

    @Override
    protected Instant readNonNull(JsonReader in) throws IOException {
        return dateAdapter.read(in).toInstant();
    }
}
