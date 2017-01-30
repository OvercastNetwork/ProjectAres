package tc.oc.pgm.mutation;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import tc.oc.api.docs.virtual.MatchDoc;

@RunWith(JUnit4.class)
public class MutationTest {

    @Test
    public void testMutationEnumsAreSynced() {
        assertTrue("Mutation enums must be equal in size", Mutation.values().length == MatchDoc.Mutation.values().length);
        for(int i = 0; i < Mutation.values().length; i++) {
            assertTrue("Mutation enums of ordinal (" + i + ") do not match", Mutation.values()[i].name().equals(MatchDoc.Mutation.values()[i].name()));
        }
    }

}
