package tc.oc.commons.util;

import java.util.stream.Stream;

import org.junit.Test;
import tc.oc.commons.core.util.Streams;

import static org.junit.Assert.*;
import static tc.oc.test.Assert.*;

public class StreamUtilsTest {

    @Test public void isUniformEmpty() throws Exception {
        assertTrue(Streams.isUniform(Stream.of()));
    }

    @Test public void isUniformSingleton() throws Exception {
        assertTrue(Streams.isUniform(Stream.of("A")));
        assertTrue(Streams.isUniform(Stream.of((Object) null)));
    }

    @Test public void isUniformHomogenous() throws Exception {
        assertTrue(Streams.isUniform(Stream.of(new String("A"), new String("A"))));
        assertTrue(Streams.isUniform(Stream.of(new String("A"), new String("A"), new String("A"))));
        assertTrue(Streams.isUniform(Stream.of(null, null)));
        assertTrue(Streams.isUniform(Stream.of(null, null, null)));
    }

    @Test public void isUniformHeterogenous() throws Exception {
        assertFalse(Streams.isUniform(Stream.of("A", "B")));
        assertFalse(Streams.isUniform(Stream.of("A", null)));

        assertFalse(Streams.isUniform(Stream.of("B", "A", "A")));
        assertFalse(Streams.isUniform(Stream.of("A", "B", "A")));
        assertFalse(Streams.isUniform(Stream.of("A", "A", "B")));
    }
}