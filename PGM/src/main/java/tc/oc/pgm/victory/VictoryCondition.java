package tc.oc.pgm.victory;

import tc.oc.pgm.match.Competitor;

/**
 * Extends {@link MatchResult} with a priority property, and the ability to
 * decide when the match is over.
 */
public interface VictoryCondition {
    /**
     * It's lame that all the subclasses need to be listed here,
     * but I can't come up with a better way to ensure they are
     * evaluated in the right order that isn't overly complex
     * or prone to bugs.
     */
    enum Priority {
        IMMEDIATE,
        TIME_LIMIT,
        SCORE,
        GOALS,
        BLITZ
    }

    Priority priority();

    /**
     * Test if this victory condition has been satisfied for the given match.
     * When this returns true, the match will end, and the lowest {@link Competitor}s
     * in the ordering will be the winners.
     */
    boolean isCompleted();

    MatchResult result();
}
