package net.anxuiz.tourney;

public enum TourneyState {
    /** Non-tournament match (default, unless teams registered) */
    DISABLED(),
    /** Tournament match, but waiting for more teams to be registered */
    ENABLED_WAITING_FOR_TEAMS(),
    /** Tournament match, but waiting for teams and spectators to ready */
    ENABLED_WAITING_FOR_READY(),
    /** Tournament match, match start countdown active */
    ENABLED_STARTING(),
    /** Tournament match, running */
    ENABLED_RUNNING(),
    /** Tournament match, complete */
    ENABLED_FINISHED(),
    /** Tournament match, in the process of selecting next match */
    ENABLED_MAP_SELECTION()
}
