package tc.oc.pgm.join;

public enum JoinMethod {
    USER,               // Normal player joined in the normal way
    FORCE,              // Forced by a staff member
    REMOTE,             // Joined remotely by the API
    PRIORITY_KICK       // Rejoin after being priority kicked
}
