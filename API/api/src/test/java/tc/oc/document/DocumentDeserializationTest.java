package tc.oc.document;

import com.google.gson.JsonObject;
import java.time.Instant;
import org.junit.Test;
import tc.oc.ApiTest;
import tc.oc.api.annotations.Serialize;

import static org.junit.Assert.*;

public class DocumentDeserializationTest extends ApiTest {

    JsonObject jsonObject = new JsonObject();

    void jsonIn(String key, Object value) {
        jsonObject.add(key, gson.toJsonTree(value));
    }

    <T> T deserialize(Class<T> type) {
        return gson.fromJson(jsonObject, type);
    }

    @Test
    public void testClassDoc() throws Exception {
        jsonIn("number", 123);
        jsonIn("text", "abc");
        ClassDoc doc = deserialize(ClassDoc.class);
        assertEquals(123, doc.number);
        assertEquals("abc", doc.text);
    }

    @Test
    public void testInterfaceDoc() throws Exception {
        jsonIn("number", 123);
        jsonIn("text", "abc");
        InterfaceDocImpl doc = deserialize(InterfaceDocImpl.class);
        assertEquals(123, doc.number());
        assertEquals("abc", doc.text());
    }

    @Test
    public void testGeneratedDoc() throws Exception {
        jsonIn("number", 123);
        jsonIn("text", "abc");
        InterfaceDoc doc = deserialize(InterfaceDoc.class);
        assertEquals(123, doc.number());
        assertEquals("abc", doc.text());
    }

    @Test
    public void testGeneratedFieldWithParameterizedTypeWhereTypeParameterHasCustomDeserializer() throws Exception {
        jsonIn("instants", new String[] { ISO_DATE });
        GenericFieldInterfaceDoc doc = deserialize(GenericFieldInterfaceDoc.class);
        assertTrue("List<Instant> was deserialized as List, generic type info was lost",
                   doc.instants().get(0) instanceof Instant);
    }
}

class InterfaceDocImpl implements InterfaceDoc {
    private int number;
    private String text;

    @Serialize public void number(int n) {number = n; }
    @Serialize public void text(String t) { text = t; }

    @Override public int number() { return number; }
    @Override public String text() { return text; }
}