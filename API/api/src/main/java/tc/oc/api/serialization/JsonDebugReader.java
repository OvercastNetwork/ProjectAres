package tc.oc.api.serialization;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.Stack;

import com.google.api.client.repackaged.com.google.common.base.Joiner;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

public class JsonDebugReader extends JsonReader {

    public static abstract class Context {}

    public static class ArrayContext extends Context {
        int index = -1;

        @Override public String toString() {
            return "[" + index + "]";
        }
    }

    public static class ObjectContext extends Context {
        String key = "(no key)";

        @Override public String toString() {
            return "." + key;
        }
    }

    private final Stack<Context> context = new Stack<>();

    /**
     * Creates a new instance that reads a JSON-encoded stream from {@code in}.
     *
     * @param reader
     */
    public JsonDebugReader(Reader reader) {
        super(reader);
    }

    public Collection<Context> getContext() {
        return context;
    }

    public String getJoinedContext() {
        return context.isEmpty() ? "(root)" : Joiner.on("").join(context);
    }

    @Override public String toString() {
        return getClass().getSimpleName() + " at " + getJoinedContext();
    }

    private void advanceArray() {
        if(context.isEmpty()) return;
        final Context c = context.lastElement();
        if(c instanceof ArrayContext) {
            ((ArrayContext) c).index++;
        }
    }

    @Override public void beginArray() throws IOException {
        super.beginArray();
        context.push(new ArrayContext());
    }

    @Override public void endArray() throws IOException {
        context.pop();
        super.endArray();
    }

    @Override public void beginObject() throws IOException {
        super.beginObject();
        context.push(new ObjectContext());
    }

    @Override public void endObject() throws IOException {
        context.pop();
        super.endObject();
    }

    @Override public String nextName() throws IOException {
        return ((ObjectContext) context.lastElement()).key = super.nextName();
    }

    @Override public String nextString() throws IOException {
        advanceArray();
        return super.nextString();
    }

    @Override public boolean nextBoolean() throws IOException {
        advanceArray();
        return super.nextBoolean();
    }

    @Override public void nextNull() throws IOException {
        advanceArray();
        super.nextNull();
    }

    @Override public double nextDouble() throws IOException {
        advanceArray();
        return super.nextDouble();
    }

    @Override public long nextLong() throws IOException {
        advanceArray();
        return super.nextLong();
    }

    @Override public int nextInt() throws IOException {
        advanceArray();
        return super.nextInt();
    }

    @Override public void skipValue() throws IOException {
        advanceArray();
        super.skipValue();
    }

    @Override public boolean hasNext() throws IOException {
        return super.hasNext();
    }

    @Override public JsonToken peek() throws IOException {
        return super.peek();
    }

    @Override public void close() throws IOException {
        super.close();
    }
}
