package tc.oc.api.serialization;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import javax.inject.Inject;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

public class JsonUtils {
    private final Gson gson;
    private final Gson prettyGson;

    @Inject JsonUtils(Gson gson, @Pretty Gson prettyGson) {
        this.gson = gson;
        this.prettyGson = prettyGson;
    }

    public String errorContext(Reader reader, Type type) {
        JsonDebugReader debugReader = new JsonDebugReader(reader);
        try {
            gson.fromJson(debugReader, type);
        } catch(JsonSyntaxException e) {
            return debugReader.getJoinedContext();
        }
        return "(no parsing error detected)";
    }

    public String errorContext(String json, Type type) {
        return errorContext(new StringReader(json), type);
    }

    public String prettify(String ugly) {
        return prettyGson.toJson(prettyGson.fromJson(ugly, JsonElement.class));
    }
}
