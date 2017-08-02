package tc.oc.pgm.score;

import java.util.Map;
import java.util.Optional;

import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.victory.AbstractVictoryCondition;

public class ScoreVictoryCondition extends AbstractVictoryCondition {

    private final Optional<Integer> scoreLimit;
    private final Map<Competitor, Double> scores;

    public ScoreVictoryCondition(Optional<Integer> scoreLimit, Map<Competitor, Double> scores) {
        super(Priority.SCORE, new ScoreMatchResult());
        this.scoreLimit = scoreLimit;
        this.scores = scores;
    }

    @Override
    public boolean isCompleted() {
        return scoreLimit.filter(
            limit -> scores.values()
                           .stream()
                           .anyMatch(score -> score >= limit)
        ).isPresent();
    }
}
