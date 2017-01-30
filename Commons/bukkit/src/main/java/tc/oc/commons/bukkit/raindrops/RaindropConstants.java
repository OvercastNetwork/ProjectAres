package tc.oc.commons.bukkit.raindrops;

import java.time.Duration;

public class RaindropConstants {
    public static final int TEAM_REWARD = 5;
    public static final double LOSING_TEAM_REWARD_PERCENT = 0.3;
    public static final Duration TEAM_REWARD_CUTOFF = Duration.ofMinutes(2);

    public static final int TOUCH_GOAL_REWARD = 5;
    public static final int WOOL_PLACE_REWARD = 10;
    public static final int WOOL_DESTROY_REWARD = 5;
    public static final int DESTROYABLE_DESTROY_PERCENT_REWARD = 10;
    public static final int KILL_REWARD = 1;

    public static final double PLAY_TIME_BONUS = 3;
    public static final int PLAY_TIME_BONUS_CUTOFF = 10;
    public static final int MATCH_FULLNESS_BONUS = 10;

    public static final int MULTIPLIER_BASE = 100;
    public static final int MULTIPLIER_MAX = 500;
    public static final int MULTIPLIER_INCREMENT = 25;
}
