package tc.oc.pgm.match;

import java.util.logging.Logger;
import javax.inject.Inject;

import tc.oc.commons.core.logging.ClassLogger;

/**
 * Scope: Match
 */
public abstract class MatchModule {

    @Inject protected Match match;

    protected Logger logger;
    @Inject void initLogger(Match match) {
        logger = ClassLogger.get(match.getLogger(), getClass());
    }

    @Deprecated
    protected MatchModule(Match match) {
        this.match = match;
        initLogger(match);
    }

    protected MatchModule() {}

    /**
     * Called before {@link #load()} to check if the module should load for this match.
     * If this returns false, the module will not be added to the context for the current
     * match, will not be registered for events or ticks, and no further callback methods
     * will be called.
     *
     * The module IS stored in the match context when this method is called, but is removed
     * if the method returns false.
     *
     * The base implementation always returns true. Naturally, if a module returns false,
     * it must ensure that its constructor does not have any unwanted side-effects.
     */
    public boolean shouldLoad() {
        return true;
    }

    /**
     * Called immediately after a match is loaded.
     *
     * The map is loaded but no players have been added yet.
     *
     * Also, note that ALL MatchModules are provisioned and injected before
     * this method is called on ANY of them. As such, a module that exports
     * an API to other modules must be in a valid state for that API to be
     * used before this method is called.
     */
    public void load() {
    }

    /**
     * Called immediately before a match is unloaded.
     * The map is still loaded but any players have already
     * been transitioned to the next match.
     */
    public void unload() {
    }

    /**
     * Called when the match starts
     */
    public void enable() {
    }

    /**
     * Called when the match ends
     */
    public void disable() {
    }

    public Logger getLogger() {
        return this.logger;
    }

    public Match getMatch() {
        return this.match;
    }
}
