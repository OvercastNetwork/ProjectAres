package tc.oc.commons.core.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import tc.oc.commons.core.ListUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

/** Tests for {@link tc.oc.commons.core.ListUtils}. */
@RunWith(JUnit4.class)
public class ListUtilsTest {
    @Test
    public void simpleListUnionTest() {
        List<String> union = ListUtils.union(Arrays.asList("hi", "hey"), Arrays.asList("lol"));
        assertTrue("[\"hi\", \"hey\"] + [\"lol\"] did not contain all elements.", union.containsAll(Arrays.asList("hi", "hey", "lol")));
    }
}
