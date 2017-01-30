package tc.oc.pgm.score;

import java.util.Optional;

public class ScoreConfig {
    public final Optional<Integer> scoreLimit;
    public final int deathScore;
    public final int killScore;

    public ScoreConfig(Optional<Integer> scoreLimit, int deathScore, int killScore) {
        this.scoreLimit = scoreLimit;
        this.deathScore = deathScore;
        this.killScore = killScore;
    }
}
