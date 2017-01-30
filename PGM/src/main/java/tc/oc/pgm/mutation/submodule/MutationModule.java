package tc.oc.pgm.mutation.submodule;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.mutation.Mutation;
import tc.oc.pgm.mutation.MutationMatchModule;

/**
 * Bits of immutable code that manage a {@link Mutation}.
 *
 * This should be able to load at any time during the match
 * and not cause any problems. This will allow mutations
 * to be forcefully loaded on the fly without any worries
 * of breaking the match state.
 *
 * TODO: Force loading is not been enabled yet, but all mutation
 * modules should be ready for the switch.
 */
@ListenerScope(MatchScope.RUNNING)
public abstract class MutationModule implements Listener {

    protected final Match match;
    protected boolean enabled;

    /**
     * Constructed when {@link MutationMatchModule#load()}
     * has been called. This will only be constructed if its
     * subsequent {@link Mutation} is enabled for the match.
     *
     * @param match the match for this module.
     */
    public MutationModule(Match match) {
        this.match = match;
        this.enabled = false;
    }

    /**
     * Called when the match starts.
     *
     * However, this should be able to be called at any
     * point before the match ends and still work as expected.
     * @param late called after the match starts.
     *
     * {@link MutationMatchModule#enable()}
     */
    public void enable(boolean late) {
        match.registerEvents(this);
        enabled = true;
    }

    /**
     * Called when the match ends.
     *
     * However, this should be able to be called at any
     * point during a match and still work as expected.
     * @param premature called before the match ends.
     *
     * {@link MutationMatchModule#disable()}
     */
    public void disable(boolean premature) {
        match.unregisterEvents(this);
        enabled = false;
    }

}
