package tc.oc.commons.bukkit.geometry;

import org.bukkit.util.ImVector;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

@RunWith(JUnit4.class)
public class CapsuleTest {

    @Test
    public void testIntersectsPoint() throws Exception {
        final Capsule C = Capsule.fromCenterAndRadius(LineSegment.of(0, 0, 0, 1, 1, 1), 0.5);
        assertTrue(C.intersects(ImVector.of(0, 0, 0)));
        assertTrue(C.intersects(ImVector.of(1, 1, 1)));
        assertTrue(C.intersects(ImVector.of(0.5, 0.5, 0.5)));
        assertTrue(C.intersects(ImVector.of(0.5, 0, 0)));
        assertFalse(C.intersects(ImVector.of(0.9, 0, 0)));
    }

    @Test
    public void testIntersectsSphere() throws Exception {
        final Capsule C = Capsule.fromCenterAndRadius(LineSegment.of(0, 0, 0, 1, 1, 1), 0.5);
        assertTrue(C.intersects(Sphere.fromCenterAndRadius(ImVector.of(1, 0, 0), 0.5)));
        assertFalse(C.intersects(Sphere.fromCenterAndRadius(ImVector.of(2, 0, 0), 0.5)));
        assertTrue(C.intersects(Sphere.fromCenterAndRadius(ImVector.of(2, 1, 2), 1)));
        assertTrue(C.intersects(Sphere.fromCenterAndRadius(ImVector.of(2, 2, 2), 1.3)));

        final Capsule LONG = Capsule.fromCenterAndRadius(LineSegment.of(0, 0, 0, 10, 10, 10), 1);
        assertTrue(LONG.intersects(Sphere.fromCenterAndRadius(ImVector.of(5, 5, 5), 1)));
        assertTrue(LONG.intersects(Sphere.fromCenterAndRadius(ImVector.of(6, 5, 5), 1)));
        assertFalse(LONG.intersects(Sphere.fromCenterAndRadius(ImVector.of(8, 5, 5), 1)));
    }
}
