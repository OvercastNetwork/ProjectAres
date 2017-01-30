package tc.oc.api.serialization;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import javax.inject.Inject;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.inject.TypeLiteral;

/**
 * Generic {@link Set<Enum>} adapter that silently skips unrecognized values when reading
 */
public class LenientEnumSetTypeAdapter<T extends Enum<T>> extends NullableTypeAdapter<Set<T>> {

    private final Class<T> type;

    @Inject public LenientEnumSetTypeAdapter(TypeLiteral<T> type) {
        this.type = (Class<T>) type.getRawType();
    }

    @Override
    protected void writeNonNull(JsonWriter out, Set<T> value) throws IOException {
        out.beginArray();
        for(T t : value) {
            out.value(t.name());
        }
        out.endArray();
    }

    @Override
    protected Set<T> readNonNull(JsonReader in) throws IOException {
        final EnumSet<T> set = EnumSet.noneOf(type);
        in.beginArray();
        while(in.hasNext()) {
            final String name = in.nextString();
            final T element;
            try {
                element = Enum.valueOf(type, name);
            } catch(IllegalArgumentException e) {
                continue;
            }
            set.add(element);
        }
        in.endArray();
        return set;
    }
}
