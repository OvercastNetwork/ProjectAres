package tc.oc.pgm.spawns;

import javax.annotation.Nullable;

import net.md_5.bungee.api.chat.BaseComponent;
import java.time.Duration;
import tc.oc.pgm.filters.Filter;

public class RespawnOptions {
    public final Duration delay;                    // Minimum wait time between death and respawn
    public final Duration freeze;
    public final long delayTicks;
    public final boolean auto;                      // Force dead players to respawn as soon as they can
    public final boolean blackout;                  // Blind dead players
    public final boolean spectate;                  // Allow dead players to fly around
    public final boolean bedSpawn;                  // Allow players to respawn from beds
    public final @Nullable BaseComponent message;   // Message to show respawning players, after the delay
    public final Filter filter;                     // Filter if this RespawnOption should be the one used

    public RespawnOptions(Duration delay, Duration freeze, boolean auto, boolean blackout, boolean spectate, boolean bedSpawn, Filter filter, @Nullable BaseComponent message) {
        this.delay = delay;
        this.delayTicks = Math.max(delay.toMillis() / 50, 20);
        this.freeze = freeze;
        this.auto = auto;
        this.blackout = blackout;
        this.spectate = spectate;
        this.bedSpawn = bedSpawn;
        this.filter = filter;
        this.message = message;
    }
}
