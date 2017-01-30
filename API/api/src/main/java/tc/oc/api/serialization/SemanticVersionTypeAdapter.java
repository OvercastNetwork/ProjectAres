package tc.oc.api.serialization;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import tc.oc.api.docs.SemanticVersion;

public class SemanticVersionTypeAdapter extends TypeAdapter<SemanticVersion> {
    @Override
    public void write(JsonWriter out, SemanticVersion version) throws IOException {
        out.beginArray();
        out.value(version.major());
        out.value(version.minor());
        if(version.patch() != 0) out.value(version.patch());
        out.endArray();
    }

    @Override
    public SemanticVersion read(JsonReader in) throws IOException {
        in.beginArray();
        int major = in.nextInt();
        int minor = in.nextInt();
        int patch = in.peek() == JsonToken.END_ARRAY ? 0 : in.nextInt();
        in.endArray();
        return new SemanticVersion(major, minor, patch);
    }
}
