package tc.oc.api.serialization;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import tc.oc.api.docs.SimplePlayerId;
import tc.oc.api.docs.PlayerId;

import java.io.IOException;

public class PlayerIdTypeAdapter extends TypeAdapter<PlayerId> {

    @Override
    public void write(JsonWriter out, PlayerId value) throws IOException {
        out
            .beginArray()
                .value(value.player_id())
                .value(value.username())
                .value(value._id())
            .endArray();
    }

    @Override
    public PlayerId read(JsonReader in) throws IOException {
        in.beginArray();
        String player_id = in.nextString();
        String username = in.nextString();
        String _id = in.nextString();
        in.endArray();

        return new SimplePlayerId(_id, player_id, username);
    }
}
