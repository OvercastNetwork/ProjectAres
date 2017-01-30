package tc.oc.pgm.start;

import net.md_5.bungee.api.chat.BaseComponent;

/**
 * Other modules can register instances of this with {@link StartMatchModule}
 * to preempt match start and display the reason to players.
 */
public interface UnreadyReason {
    /**
     * Why the match cannot start
     */
    BaseComponent getReason();

    /**
     * Can the match be forced to start by a user command, despite this reason?
     * If this is false, there is no way for any user to override this reason
     * and start the match.
     */
    boolean canForceStart();
}
