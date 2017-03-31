package tc.oc.pgm.blitz;

import tc.oc.pgm.victory.AbstractVictoryCondition;

public class BlitzVictoryCondition extends AbstractVictoryCondition {

    private final BlitzMatchModuleImpl blitz;

    protected BlitzVictoryCondition(BlitzMatchModuleImpl blitz) {
        super(Priority.BLITZ, new BlitzMatchResult());
        this.blitz = blitz;
    }

    @Override
    public boolean isCompleted() {
        // At least one competitor must be eliminated before the match can end.
        // This allows maps to be tested with one or zero competitors present.
        final int count = blitz.remainingCompetitors();
        return blitz.activated() && count <= 1 && count < blitz.competitors();
    }

}
