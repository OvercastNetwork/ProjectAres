package tc.oc.pgm.mutation.types.kit;

import tc.oc.pgm.doublejump.DoubleJumpKit;
import tc.oc.pgm.match.Match;

public class JumpMutation extends NoFallMutation {

    final static DoubleJumpKit JUMP = new DoubleJumpKit();

    public JumpMutation(Match match) {
        super(match, false, JUMP);
    }

}
