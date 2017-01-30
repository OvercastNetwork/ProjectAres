package tc.oc.pgm.teams;

import javax.annotation.Nullable;

import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.Party;

public final class Teams {

    private Teams() {}

    public static @Nullable Team get(Party party) {
        return party instanceof Team ? (Team) party : null;
    }

    public static @Nullable Team get(MatchPlayer player) {
        return player.hasParty() ? get(player.getParty()) : null;
    }

    public static @Nullable TeamFactory getDefinition(Party party) {
        return party instanceof Team ? ((Team) party).getInfo() : null;
    }
}
