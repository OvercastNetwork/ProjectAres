package tc.oc.api.serialization;

import java.io.IOException;
import java.util.UUID;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import tc.oc.api.util.UUIDs;

public class UuidTypeAdapter extends NullableTypeAdapter<UUID> {
    @Override
    protected void writeNonNull(JsonWriter out, UUID value) throws IOException {
        out.value(UUIDs.normalize(value));
    }

    @Override
    protected UUID readNonNull(JsonReader in) throws IOException {
        return UUIDs.parse(in.nextString());
    }
}
