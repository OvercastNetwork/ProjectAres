package tc.oc.pgm.flag.state;

import java.util.Collections;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.ParticipantState;
import tc.oc.pgm.match.Party;
import tc.oc.pgm.flag.Flag;
import tc.oc.pgm.flag.Post;
import tc.oc.pgm.goals.SimpleGoal;

// Flag has been permanently captured
public class Completed extends Returned {

    public Completed(Flag flag, Post post) {
        super(flag, post, null);
    }

    @Override
    public Iterable<Location> getProximityLocations(ParticipantState player) {
        return Collections.emptySet();
    }

    @Override
    protected boolean canPickup(MatchPlayer player) {
        return false;
    }

    @Override
    public ChatColor getStatusColor(Party viewer) {
        return SimpleGoal.COLOR_COMPLETE;
    }

    @Override
    public String getStatusSymbol(Party viewer) {
        return SimpleGoal.SYMBOL_COMPLETE;
    }
}
