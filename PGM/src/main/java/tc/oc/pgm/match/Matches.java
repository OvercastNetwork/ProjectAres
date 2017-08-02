package tc.oc.pgm.match;

import javax.annotation.Nullable;

import org.bukkit.Physical;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import tc.oc.pgm.PGM;

public class Matches {
    private Matches() {}

    public static @Nullable Match get(World world) {
        if(world == null) return null;
        final MatchManager mm = PGM.getMatchManager();
        return mm == null ? null : mm.getMatch(world);
    }

    public static @Nullable Match get(Physical physical) {
        return physical == null ? null : get(physical.getWorld());
    }

    public static @Nullable Match get(Event event) {
        return event instanceof Physical ? get((Physical) event) : null;
    }

    public static Match get(CommandSender sender) {
        if(sender instanceof Physical) {
            return get((Physical) sender);
        } else {
            final MatchManager mm = PGM.getMatchManager();
            return mm == null ? null : mm.getCurrentMatch();
        }
    }
}
