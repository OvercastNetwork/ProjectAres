package tc.oc.pgm.teams;

import java.util.Optional;

import tc.oc.pgm.join.JoinAllowed;

public class JoinTeam extends JoinAllowed {

    public JoinTeam(Team team, boolean rejoin, boolean priorityKick) {
        super(Optional.of(team), rejoin, priorityKick);
    }
}
