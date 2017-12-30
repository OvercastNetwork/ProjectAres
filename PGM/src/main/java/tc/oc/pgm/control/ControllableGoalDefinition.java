package tc.oc.pgm.control;

import tc.oc.pgm.features.GamemodeFeature;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.goals.GoalDefinition;
import tc.oc.pgm.teams.TeamFactory;

import java.time.Duration;
import java.util.Optional;

public interface ControllableGoalDefinition extends GoalDefinition, GamemodeFeature {

    /**
     * Which players are allowed to control the goal.
     */
    Filter captureFilter();

    /**
     * Which players are allowed to defend (revert progress) on the goal.
     */
    Filter defendFilter();

    /**
     * Base time for the goal to transition between states.
     */
    Duration captureTime();

    /**
     * Multiplier applied to capture time based on the number of players dominating the goal.
     */
    double multiplierTime();

    /**
     * Relative rate at which progress reverts while nobody is dominating the goal.
     */
    double decayRate();

    /**
     * Relative rate at which progress reverts from players dominating the goal.
     */
    double recoveryRate();

    /**
     * Relative rate at which progress will transition to neutral even if a neutral state if false.
     */
    double neutralRate();

    /**
     * The team that initially controls the goal before the match starts.
     */
    Optional<TeamFactory> initialOwner();

    /**
     * Condition for when the goal transitions from one controller to another.
     */
    CaptureCondition captureCondition();

    enum CaptureCondition {
        EXCLUSIVE,  // Team owns all players on the point
        MAJORITY,   // Team owns more than half the players on the point
        LEAD;       // Team owns more players on the point than any other single team
    }

    /**
     * Whether the goal transitions to neural control before a team control.
     */
    boolean neutralState();

    /**
     * Whether goal transitions are immutable and cannot be reverted.
     */
    boolean permanent();

    /**
     * Whether active ownership of the goal gives points to the controlling team.
     */
    default boolean affectsScore() {
        return pointsPerSecond() != 0;
    }

    /**
     * Amount of points given to a team once they control the goal.
     * Those points are revoked if they loose ownership.
     */
    float pointsOwned();

    /**
     * Amount of points given to the controlling team every second.
     */
    float pointsPerSecond();

    /**
     * After this many seconds pass in the match, the points per second will double exponentially.
     */
    float pointsGrowth();

    /**
     * Whether progress of controlling the goal is published and visible.
     */
    boolean showProgress();

}
