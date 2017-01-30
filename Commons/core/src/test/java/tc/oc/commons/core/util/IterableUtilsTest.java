package tc.oc.commons.core.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import tc.oc.commons.core.IterableUtils;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/** Tests for {@link tc.oc.commons.core.IterableUtils}. */
@RunWith(JUnit4.class)
public class IterableUtilsTest {
    @Test
    public void simpleIterableMostCommonWithTieTest() {
        assertEquals("[\"hey\", \"hey\", \"hi\", \"lol\", \"hi\", \"hi\"] did not evaluate to \"hi\"", "hi",
                IterableUtils.findMostCommon(Arrays.asList(
                        "hey",
                        "hey",
                        "hi",
                        "lol",
                        "hi",
                        "hi"))
        );
    }

    @Test
    public void simpleIterableMostCommonWithOutTieTest() {
        assertNull("[\"hey\", \"hey\", \"hey\", \"hi\", \"lol\", \"hi\", \"hi\"] did not evaluate to null",
                IterableUtils.findMostCommon(
                        Arrays.asList(
                                "hey",
                                "hey",
                                "hey",
                                "hi",
                                "lol",
                                "hi",
                                "hi"),
                        false)
        );
    }
}
