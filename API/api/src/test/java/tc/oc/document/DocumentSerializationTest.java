package tc.oc.document;

import java.lang.reflect.Type;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.Test;
import tc.oc.ApiTest;
import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.virtual.Document;

import static org.junit.Assert.*;

public class DocumentSerializationTest extends ApiTest {

    String jsonText;
    JsonElement jsonElement;
    JsonObject jsonObject;

    void serialize(Object obj, Type type) {
        jsonElement = gson.toJsonTree(obj, type);
        jsonText = gson.toJson(jsonElement);
        if(jsonElement instanceof JsonObject) jsonObject = (JsonObject) jsonElement;
    }

    void serialize(Object obj) {
        if(obj != null) serialize(obj, obj.getClass());
    }

    void assertJsonOut(String key, Object value) {
        assertEquals(gson.toJsonTree(value), jsonObject.get(key));
    }

    @Test
    public void testGsonSerializesAnonymousClasses() throws Exception {
        String json = gson.toJson(new Object() { int number = 123; });
        assertEquals("Gson refused to serialize an anonymous class (are you using our custom fork?)",
                     "{\"number\":123}", json);
    }

    @Test
    public void testPlain() throws Exception {
        serialize(new ClassDoc(123, "abc"));
        assertJsonOut("number", 123);
        assertJsonOut("text", "abc");
    }

    @Test
    public void testAnonymous() throws Exception {
        serialize(new Document() {
            @Serialize int number = 123;
            @Serialize String text = "abc";
        });
        assertJsonOut("number", 123);
        assertJsonOut("text", "abc");
    }

    @Test
    public void testInherited() throws Exception {
        serialize(new InterfaceDoc() {
            @Override public int number() { return 123; }
            @Override public String text() { return "abc"; }
        });
        assertJsonOut("number", 123);
        assertJsonOut("text", "abc");
    }

    @Test
    public void testParameterized() throws Exception {
        serialize(new GenericInterfaceDoc<Integer>() {
            @Override public Integer value() { return 123; }
            @Override public List<Integer> values() { return ImmutableList.of(1, 2, 3); }
        });
        assertJsonOut("value", 123);
        assertJsonOut("values", new int[]{1, 2, 3});
    }

    @Test
    public void testParameterizedComplex() throws Exception {
        serialize(new GenericInterfaceDoc<List<Integer>>() {
            @Override public List<Integer> value() {
                return ImmutableList.of(1, 2, 3);
            }

            @Override public List<List<Integer>> values() {
                return ImmutableList.<List<Integer>>of(
                    ImmutableList.of(1, 2, 3),
                    ImmutableList.of(4, 5, 6)
                );
            }
        });
        assertJsonOut("value", new int[]{1, 2, 3});
        assertJsonOut("values", new int[][]{new int[]{1, 2, 3}, new int[]{4, 5, 6}});
    }
}
