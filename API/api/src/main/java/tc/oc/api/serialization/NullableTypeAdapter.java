package tc.oc.api.serialization;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * {@link TypeAdapter} base that handles null values in the typical fashion.
 * Gson has {@link #nullSafe()} for this purpose, but it seems safer to
 * make it part of the adapter itself rather than assuming it will be wrapped.
 */
public abstract class NullableTypeAdapter<T> extends TypeAdapter<T> {

    protected abstract void writeNonNull(JsonWriter out, T value) throws IOException;

    protected abstract T readNonNull(JsonReader in) throws IOException;

    @Override
    final public void write(JsonWriter out, T value) throws IOException {
      if (value == null) {
        out.nullValue();
      } else {
        writeNonNull(out, value);
      }
    }

    @Override
    final public T read(JsonReader reader) throws IOException {
      if (reader.peek() == JsonToken.NULL) {
        reader.nextNull();
        return null;
      }
      return readNonNull(reader);
    }
}
