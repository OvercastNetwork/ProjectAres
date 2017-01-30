package tc.oc.api.serialization;

import java.io.IOException;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.time.Duration;

public class DurationTypeAdapter extends NullableTypeAdapter<Duration> {

    @Override
    public void writeNonNull(JsonWriter out, Duration duration) throws IOException {
        out.value(duration.toMillis());
    }

    @Override
    public Duration readNonNull(JsonReader in) throws IOException {
        return Duration.ofMillis(in.nextLong());
    }
}
