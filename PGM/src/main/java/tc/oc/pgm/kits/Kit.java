package tc.oc.pgm.kits;

import org.bukkit.entity.Player;
import tc.oc.pgm.features.FeatureDefinition;
import tc.oc.pgm.features.FeatureInfo;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;

@FeatureInfo(name = "kit")
public interface Kit extends FeatureDefinition {
    /**
     * Apply this kit to the given player. If force is true, the player's state is made
     * to match the kit as strictly as possible, otherwise the kit may be given to the
     * player in a way that is more in their best interest. Subclasses will interpret
     * these concepts in their own way.
     *
     * A mutable List must be given, to which the Kit may add ItemStacks that could not
     * be applied normally, because the player's inventory was full. These stacks will
     * be given to the player using the natural give algorithm after ALL kits have been
     * applied. This phase must be deferred in this way so that overflow from one kit
     * does not displace stacks in another kit applied simultaneously. In this way, the
     * number of stacks that go to their proper slots is maximized.
     */
    void apply(MatchPlayer player, boolean force, ItemKitApplicator items);

    default void remove(MatchPlayer player) {
        throw new UnsupportedOperationException(this + " is not removable");
    }

    default boolean isRemovable() {
        return false;
    }

    default void apply(MatchPlayer player) {
        apply(player, false);
    }

    default void apply(MatchPlayer player, boolean force) {
        final ItemKitApplicator items = new ItemKitApplicator();
        apply(player, force, items);
        items.apply(player);

        /**
         * When max health is lowered by an item attribute or potion effect, the client can
         * go into an inconsistent state that has strange effects, like the death animation
         * playing when the player isn't dead. It is probably related to this bug:
         *
         * https://bugs.mojang.com/browse/MC-19690
         *
         * This appears to fix the client state, for reasons that are unclear. The one tick
         * delay is necessary. Any less and getMaxHealth will not reflect whatever was
         * applied in the kit to modify it.
         */
        final Player bukkit = player.getBukkit();
        player.getMatch().getScheduler(MatchScope.LOADED).createDelayedTask(1, () -> {
            if(bukkit.isOnline() && !player.isDead() && bukkit.getMaxHealth() < 20) {
                bukkit.setHealth(Math.min(bukkit.getHealth(), bukkit.getMaxHealth()));
            }
        });
    }

    abstract class Impl extends FeatureDefinition.Impl implements Kit {}
}
