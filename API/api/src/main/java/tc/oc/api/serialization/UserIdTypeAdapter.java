package tc.oc.api.serialization;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import tc.oc.api.docs.SimpleUserId;
import tc.oc.api.docs.UserId;

import java.io.IOException;

public class UserIdTypeAdapter extends TypeAdapter<UserId> {

    @Override
    public void write(JsonWriter out, UserId value) throws IOException {
        out.value(value.toString());
    }

    @Override
    public UserId read(JsonReader in) throws IOException {
        return new SimpleUserId(in.nextString());
    }
}
