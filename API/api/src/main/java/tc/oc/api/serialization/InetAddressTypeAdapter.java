package tc.oc.api.serialization;

import java.io.IOException;
import java.net.InetAddress;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class InetAddressTypeAdapter extends NullableTypeAdapter<InetAddress> {
    @Override
    protected void writeNonNull(JsonWriter out, InetAddress value) throws IOException {
        out.value(value.getHostAddress());
    }

    @Override
    protected InetAddress readNonNull(JsonReader in) throws IOException {
        return InetAddress.getByName(in.nextString());
    }
}
