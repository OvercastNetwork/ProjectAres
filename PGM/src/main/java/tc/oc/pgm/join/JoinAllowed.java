package tc.oc.pgm.join;

import java.util.Optional;

import tc.oc.pgm.match.Competitor;

public class JoinAllowed implements JoinResult {

    private final Optional<? extends Competitor> competitor;
    private final boolean rejoin;
    private final boolean priorityKick;

    protected JoinAllowed(Optional<? extends Competitor> competitor, boolean rejoin, boolean priorityKick) {
        this.competitor = competitor;
        this.rejoin = rejoin;
        this.priorityKick = priorityKick;
    }

    public static JoinAllowed auto(boolean priorityKick) {
        return new JoinAllowed(Optional.empty(), false, priorityKick);
    }

    public static JoinAllowed force(JoinResult result) {
        return new JoinAllowed(result.competitor(), result.isRejoin(), result.priorityKickRequired());
    }

    @Override
    public boolean priorityKickRequired() {
        return priorityKick;
    }

    @Override
    public Optional<? extends Competitor> competitor() {
        return competitor;
    }

    @Override
    public boolean isRejoin() {
        return rejoin;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public boolean isAllowed() {
        return true;
    }
}
