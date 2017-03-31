package tc.oc.pgm.blitz;

import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.MatchPlayer;

import javax.annotation.Nullable;
import java.util.Optional;

public interface BlitzMatchModule {

    /**
     * Get the properties for the blitz module.
     *
     * It may change during a match from {@link #activate(BlitzProperties)}.
     */
    BlitzProperties properties();

    /**
     * Is the blitz module *really* activated?
     *
     * Since the module is always loaded on the chance it should
     * be activated during a match, it is only actively enforcing
     * its rules when this returns true.
     */
    boolean activated();

    /**
     * Activate the blitz module with a new set of properties.
     *
     * If the properties are null, it will default to its current
     * {@link #properties()}.
     */
    void activate(@Nullable BlitzProperties properties);

    default void activate() {
        activate(null);
    }

    /**
     * Deactivate the blitz module by clearing all of its data.
     *
     * The module can be activated and deactivated as many times
     * as you need.
     */
    void deactivate();

    /**
     * Increment the number of lives for a player if
     * {@link #eliminated(MatchPlayer)} is false.
     *
     * If notify is true, the player will get a message
     * explaining how much lives they gained or lost.
     *
     * If immediate is true and the player's lives are empty,
     * {@link #eliminate(MatchPlayer)} will be called and the
     * player will be out of the game.
     *
     * @return Whether the player is now {@link #eliminated(MatchPlayer)}.
     */
    boolean increment(MatchPlayer player, int lives, boolean notify, boolean immediate);

    /**
     * Is the player eliminated from the match?
     *
     * This value can change back to false if the player were
     * forced onto another team after being eliminated.
     */
    boolean eliminated(MatchPlayer player);

    /**
     * Eliminate the player from this match by moving
     * them to the default party and preventing them from respawning.
     */
    void eliminate(MatchPlayer player);

    /**
     * Try to get the lives for this player.
     */
    Optional<Lives> lives(MatchPlayer player);

    /**
     * Get the amount of lives a player has left.
     *
     * @throws IllegalStateException if {@link #lives(MatchPlayer)} is not present.
     */
    int livesCount(MatchPlayer player);

    /**
     * Try to get the team lives for this competitor.
     */
    Optional<Lives> lives(Competitor competitor);

}
