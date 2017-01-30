package tc.oc.document;

import java.util.Collections;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import org.junit.Test;
import tc.oc.ApiTest;
import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.virtual.BasicDocument;
import tc.oc.api.docs.virtual.Document;
import tc.oc.api.document.DocumentGenerator;
import tc.oc.api.document.DocumentRegistry;

import static org.junit.Assert.*;
import static tc.oc.test.Assert.*;

@Serialize interface Empty extends Document {}
@Serialize interface RequiredPrimitive extends Document { int woot(); }
@Serialize interface PrimitiveWithDefault extends Document { default int woot() { return 123; } }
@Serialize interface NullablePrimitive extends Document { @Nullable Integer woot(); }
@Serialize interface NonNullObject extends Document { @Nonnull String woot(); }

public class DocumentGeneratorTest extends ApiTest {

    @Inject DocumentRegistry registry;
    @Inject DocumentGenerator generator;

    @Test
    public void testSimpleDocument() throws Exception {
        final RequiredPrimitive doc = registry.instantiate(RequiredPrimitive.class, Collections.singletonMap("woot", 123));
        assertEquals(123, doc.woot());
    }

    @Test
    public void testMissingPrimitive() throws Exception {
        try {
            registry.instantiate(RequiredPrimitive.class, Collections.emptyMap());
            fail();
        } catch(IllegalArgumentException e) {
            // pass
        }
    }

    @Test
    public void testNullPrimitive() throws Exception {
        try {
            registry.instantiate(RequiredPrimitive.class, Collections.singletonMap("woot", null));
            fail();
        } catch(NullPointerException e) {
            // pass
        }
    }

    @Test
    public void testNonNullObjectWithExplicitNull() throws Exception {
        try {
            registry.instantiate(NonNullObject.class, Collections.singletonMap("woot", null));
            fail();
        } catch(NullPointerException e) {
            // pass
        }
    }

    @Test
    public void testMissingNonNullObject() throws Exception {
        try {
            registry.instantiate(NonNullObject.class, Collections.emptyMap());
            fail();
        } catch(IllegalArgumentException e) {
            // pass
        }
    }

    @Test
    public void testPrimitiveTypeValidation() throws Exception {
        try {
            registry.instantiate(RequiredPrimitive.class, Collections.singletonMap("woot", "lol"));
            fail();
        } catch(ClassCastException e) {
            // pass
        }
    }

    @Test
    public void testPrimitiveDefault() throws Exception {
        final PrimitiveWithDefault doc = registry.instantiate(PrimitiveWithDefault.class, Collections.emptyMap());
        assertEquals(123, doc.woot());
    }

    @Test
    public void testValueForPrimitiveDefault() throws Exception {
        final PrimitiveWithDefault doc = registry.instantiate(PrimitiveWithDefault.class, Collections.singletonMap("woot", 456));
        assertEquals(456, doc.woot());
    }

    @Test
    public void testPrimitiveDefaultWithExplicitNull() throws Exception {
        try {
            registry.instantiate(PrimitiveWithDefault.class, Collections.singletonMap("woot", null));
            fail();
        } catch(NullPointerException e) {
            // pass
        }
    }

    @Test
    public void testNullablePrimitiveWithExplicitNull() throws Exception {
        final NullablePrimitive doc = registry.instantiate(NullablePrimitive.class, Collections.singletonMap("woot", null));
        assertNull(doc.woot());
    }

    @Test
    public void testNullablePrimitiveWithImplicitNull() throws Exception {
        final NullablePrimitive doc = registry.instantiate(NullablePrimitive.class, Collections.emptyMap());
        assertNull(doc.woot());
    }

    @Test
    public void testBaseMethod() throws Exception {
        final BasicDocument base = new BasicDocument();
        final int code = generator.instantiate(registry.getMeta(Empty.class), base, Collections.emptyMap()).hashCode();
        assertEquals(base.hashCode(), code);
    }
}

