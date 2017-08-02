package tc.oc.commons.bukkit.geometry;

import org.bukkit.util.ImVector;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class LineSegmentTest {

    private static final double SMIDGE = 0.00000001;

    private static final ImVector
        A = ImVector.of(1, 2, 3),
        B = ImVector.of(4, 5, 6);

    private static final LineSegment L = LineSegment.between(A, B);

    private static void assertRoughly(double expected, double actual) {
        assertEquals(expected, actual, SMIDGE);
    }

    @Test
    public void testParametericPoint() throws Exception {
        assertTrue(L.delta().dot(L.parametricPoint(-1)) < 0);
        assertEquals(A, L.parametricPoint(0));
        assertEquals(A.plus(B).times(0.5), L.parametricPoint(0.5));
        assertEquals(B, L.parametricPoint(1));
        assertTrue(L.delta().dot(L.parametricPoint(2)) > 0);
    }

    @Test
    public void testPerpendicularProjection() throws Exception {
        final LineSegment X = LineSegment.of(0, 0, 0, 1, 0, 0);
        assertRoughly(-1, X.perpendicularProjectionParameter(ImVector.of(-1, 0, 0)));
        assertRoughly(0, X.perpendicularProjectionParameter(ImVector.of(0, 0, 0)));
        assertRoughly(1, X.perpendicularProjectionParameter(ImVector.of(1, 0, 0)));
        assertRoughly(2, X.perpendicularProjectionParameter(ImVector.of(2, 0, 0)));

        final LineSegment DIAG = LineSegment.of(0, 0, 0, 1, 1, 1);
        assertRoughly(1D / 3D, DIAG.perpendicularProjectionParameter(ImVector.of(1, 0, 0)));
        assertRoughly(2D / 3D, DIAG.perpendicularProjectionParameter(ImVector.of(1, 1, 0)));
    }

    @Test
    public void testDistanceFromPoint() throws Exception {
        final LineSegment DIAG = LineSegment.of(0, 0, 0, 1, 0, 1);
        assertRoughly(Math.sqrt(2) / 2, DIAG.distance(ImVector.of(1, 0, 0)));
        assertRoughly(Math.sqrt(2), DIAG.distance(ImVector.of(2, 0, 0)));
        assertRoughly(Math.sqrt(5), DIAG.distance(ImVector.of(3, 0, 0)));
        assertRoughly(1, DIAG.distance(ImVector.of(-1, 0, 0)));
    }
}
